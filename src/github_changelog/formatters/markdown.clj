(ns github-changelog.formatters.markdown
  (:require
    [github-changelog.util :refer [str-map]]
    [github-changelog.schema :refer [Tag Change ChangeType Fn Semver]]
    [github-changelog.markdown :as markdown]
    [clojure.string :refer [join]]
    [clojure.core.match :refer [match]]
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

(s/defn translate-type :- s/Str
  [type :- s/Str]
  (get type-name-map (keyword type) type))

(s/defn format-scope :- s/Str
  [scope :- s/Str]
  (markdown/emphasis (str scope ":")))

(s/defn format-change :- s/Str
  [change :- Change]
  (str
    (:subject change)
    (let [pr (:pull-request change)]
      (str " " (markdown/link (str "#" (:number pr)) (:html_url pr))))
    (if-let [issues (seq (:issues change))]
      (str ", closes " (join ", " (map (partial apply markdown/link) issues))))))

(defmulti format-grouped-changes #(count (second %)))

(s/defmethod format-grouped-changes 1 :- String
             [[scope changes :- [Change]]]
             (str (format-scope scope)
                  " "
                  (format-change (first changes))))

(s/defmethod format-grouped-changes :default :- String
             [[scope changes :- [Change]]]
             (str (format-scope scope)
                  (markdown/ul (map format-change changes))))

(s/defn format-changes :- s/Str
  [[type changes :- [Change]]]
  (str (markdown/h4 (translate-type type))
       (markdown/ul (map format-grouped-changes (group-by :scope changes)))))

(s/defn highlight-fn :- Fn
  [version :- Semver]
  (match (mapv version [:minor :patch :pre-release :build])
         [0 0 nil nil] markdown/h1
         [_ 0 nil nil] markdown/h2
         [_ _ nil nil] markdown/h3
         [_ _ _ nil] markdown/h4
         :else markdown/h5))

(s/defn format-tag :- s/Str
  [tag :- Tag]
  (str ((highlight-fn (:version tag)) (:name tag))
       (str-map format-changes (group-by :type (:changes tag)))))

(s/defn format-tags :- s/Str
  "Generates a markdown version from the changes"
  [tags :- [Tag]]
  (str-map format-tag tags))
