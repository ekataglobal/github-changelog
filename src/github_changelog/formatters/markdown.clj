(ns github-changelog.formatters.markdown
  (:require
    [github-changelog.util :refer [str-map]]
    [github-changelog.schema :refer [Tag Change ChangeType Fn Semver]]
    [github-changelog.markdown :as markdown]
    [github-changelog.semver :refer [get-type]]
    [clojure.string :refer [join]]
    [schema.core :as s]))

(def type-name-map
  {:feat "Features"
   :fix "Bug Fixes"
   :perf "Performance Improvements"
   :docs "Documentations"
   :chore "Chores"
   :style "Style Changes"
   :refactor "Refactorings"
   :test "Tests"})

(defn- translate-type [type]
  (get type-name-map (keyword type) type))

(defn- format-scope [scope]
  (markdown/emphasis (str scope ":")))

(defn- format-change [change]
  (str
    (:subject change)
    (let [pr (:pull-request change)]
      (str " " (markdown/link (str "#" (:number pr)) (:html_url pr))))
    (if-let [issues (seq (:issues change))]
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

(s/defn format-changes :- s/Str
  [[type changes :- [Change]]]
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

(s/defn format-tag :- s/Str
  [tag :- Tag]
  (str ((highlight-fn (:version tag)) (:name tag))
       (str-map format-changes (group-by :type (:changes tag)))))

(s/defn format-tags :- s/Str
  "Generates a markdown version from the changes"
  [tags :- [Tag]]
  (str-map format-tag tags))
