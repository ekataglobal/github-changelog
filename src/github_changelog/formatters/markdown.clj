(ns github-changelog.formatters.markdown
  (:require [clojure.string :as str]
            [github-changelog.markdown :as md]
            [github-changelog.semver :as semver]
            [github-changelog.util :as util]))

(defmulti translate-type identity)

(doseq [[k v] {"feat"     "Features"
               "fix"      "Bug Fixes"
               "perf"     "Performance Improvements"
               "docs"     "Documentations"
               "chore"    "Chores"
               "style"    "Style Changes"
               "refactor" "Refactorings"
               "test"     "Tests"}]
  (defmethod translate-type k [_] v))

(defmethod translate-type :default [x] x)

(defn- format-type [type]
  (md/h4 (translate-type type)))

(defn- format-scope [scope]
  (when (seq scope)
    (md/emphasis (str scope ":"))))

(defn- format-pull-request [{:keys [number html_url]}]
  (str " " (md/link (str "#" number) html_url)))

(defn- format-change [{:keys [subject pull-request issues]}]
  (str
   subject
   (format-pull-request pull-request)
   (if-let [issues (seq issues)]
     (str ", closes " (str/join ", " (map (partial apply md/link) issues))))))

(defn- format-entries [changes]
  (str/join (map md/li changes)))

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

(defn format-change-group [[type changes]]
  (str (format-type type)
       (->> (group-by :scope changes)
            (map map-formatted)
            (map format-grouped-changes)
            (flatten)
            (format-entries))))

(defmulti highlight-fn semver/get-type)

(defmethod highlight-fn :major [_] (comp md/h2 md/emphasis))
(defmethod highlight-fn :minor [_] md/h2)
(defmethod highlight-fn :patch [_] (comp md/h3 md/emphasis))
(defmethod highlight-fn :pre-release [_] md/h3)
(defmethod highlight-fn :default [_] (comp md/h4 md/emphasis))

(defn- format-version [version name]
  ((highlight-fn version) name))

(defn format-changes [changes]
  (util/str-map format-change-group (group-by :type changes)))

(defn format-tag [{:keys [version name changes]}]
  (str (format-version version name)
       (format-changes changes)))

(defn format-tags
  "Generates a markdown version from the changes"
  [tags {:keys [collapse-tags]}]
  (if collapse-tags
    (format-changes (mapcat :changes tags))
    (util/str-map format-tag tags)))
