(ns github-changelog.formatters.markdown
  (:require [clojure.string :refer [join]]
            [github-changelog
             [markdown :as markdown]
             [semver :refer [get-type]]
             [util :refer [str-map]]]))

(defmulti translate-type identity)

(doseq [[k v] {"feat" "Features"
               "fix" "Bug Fixes"
               "perf" "Performance Improvements"
               "docs" "Documentations"
               "chore" "Chores"
               "style" "Style Changes"
               "refactor" "Refactorings"
               "test" "Tests"}]
  (defmethod translate-type k [_] v))

(defmethod translate-type :default [x] x)

(defn- format-scope [scope]
  (when (not-empty scope)
    (markdown/emphasis (str scope ":"))))

(defn format-pull-request [{:keys [number html_url]}]
  (str " " (markdown/link (str "#" number) html_url)))

(defn format-change [{:keys [subject pull-request issues]}]
  (str
   subject
   (format-pull-request pull-request)
   (if-let [issues (seq issues)]
     (str ", closes " (join ", " (map (partial apply markdown/link) issues))))))

(defn- format-entries [changes]
  (join (map markdown/li changes)))

(defmulti format-grouped-changes (comp count second))

(defmethod format-grouped-changes 1 [[scope [change]]]
  (if (empty? scope)
    change
    (str scope " " change)))

(defmethod format-grouped-changes :default [[scope changes]]
  (if (empty? scope)
    changes
    (str scope (format-entries changes))))

(defn- map-formatted [[scope changes]]
  [(format-scope scope) (map format-change changes)])

(defn format-changes [[type changes]]
  (str (markdown/h6 (translate-type type))
       (->> (group-by :scope changes)
            (map map-formatted)
            (map format-grouped-changes)
            format-entries)))

(defmulti highlight-fn get-type)

(defmethod highlight-fn :major [_] markdown/h1)
(defmethod highlight-fn :minor [_] markdown/h2)
(defmethod highlight-fn :patch [_] markdown/h3)
(defmethod highlight-fn :pre-release [_] markdown/h4)
(defmethod highlight-fn :default [_] markdown/h5)

(defn format-tag [{:keys [version name changes]}]
  (str ((highlight-fn version) name)
       (str-map format-changes (group-by :type changes))))

(defn format-tags
  "Generates a markdown version from the changes"
  [tags]
  (str-map format-tag tags))
