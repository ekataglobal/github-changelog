(ns github-changelog.formatters.markdown
  (:require
    [github-changelog.util :refer [str-map]]
    [github-changelog.markdown :as markdown]
    [clojure.string :refer [join]]
    [clojure.core.match :refer [match]]))

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
       (markdown/ul (map format-change changes))))

(defn format-changes [[type changes]]
  (str (markdown/h4 (translate-type type))
       (markdown/ul (map format-grouped-changes (group-by :scope changes)))))

(defn highlight-fn [version]
  (match (mapv version [:minor :patch :pre-release :build])
         [0 0 nil nil] markdown/h1
         [_ 0 nil nil] markdown/h2
         [_ _ nil nil] markdown/h3
         [_ _ _ nil] markdown/h4
         :else markdown/h5))

(defn format-tag [{:keys [version name changes]}]
  (str ((highlight-fn version) name)
       (str-map format-changes (group-by :type changes))))

(defn format-tags
  "Generates a markdown version from the changes"
  [tags]
  (str-map format-tag tags))
