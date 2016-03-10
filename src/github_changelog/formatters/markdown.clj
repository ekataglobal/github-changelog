(ns github-changelog.formatters.markdown
  (:require
   [github-changelog.util :refer [str-map]]
   [github-changelog.markdown :as markdown]
   [github-changelog.semver :refer [get-type]]
   [clojure.string :refer [join]]))

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

(defn format-scope [scope]
  (markdown/emphasis (str scope ":")))

(defn format-pull-request [{:keys [number html_url]}]
  (str " " (markdown/link (str "#" number) html_url)))

(defn format-change [{:keys [subject pull-request issues]}]
  (str
   subject
   (format-pull-request pull-request)
   (if-let [issues (seq issues)]
     (str ", closes " (join ", " (map (partial apply markdown/link) issues))))))

(defmulti format-grouped-changes #(count (second %)))

(defmethod format-grouped-changes 1 [[scope changes]]
  (str (format-scope scope)
       " "
       (format-change (first changes))))

(defmethod format-grouped-changes :default [[scope changes]]
  (str (format-scope scope)
       (->> (map format-change changes)
            (map markdown/li)
            join)))

(defn format-changes [[type changes]]
  (str (markdown/h5 (translate-type type))
       (->> (group-by :scope changes)
            (map format-grouped-changes)
            (map markdown/li)
            join)))

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
