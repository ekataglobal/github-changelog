(ns hu.ssh.github-changelog.formatters.markdown
  (:require
    [hu.ssh.github-changelog.util :refer [str-map]]
    [hu.ssh.github-changelog.schema :refer [Tag Change ChangeType]]
    [hu.ssh.github-changelog.markdown :as markdown]
    [schema.core :as s]))

(def type-name-map
  {:fix "Bug Fixes"
   :chore "Chores"
   :feat "Features"})

(s/defn translate-type :- s/Str
  [type :- s/Str]
  (get type-name-map (keyword type) type))

(s/defn format-change :- s/Str
  [change :- Change]
  (str (markdown/emphasis (:scope change))
       " "
       (:subject change)
       (let [issues (:issues change)]
         (if (not (empty? issues))
           (str ", closes " (str-map (partial apply markdown/link) issues)))))

(s/defn format-changes :- s/Str
  [[type changes :- [Change]]]
  (str (markdown/h4 (translate-type type))
       (markdown/ul (map format-change changes))))

(s/defn format-tag :- s/Str
  [tag :- Tag]
  (str (markdown/h3 (:name tag))
       (str-map format-changes (group-by :type (:changes tag)))))

(s/defn format-tags :- s/Str
  "Generates a markdown version from the changes"
  [tags :- [Tag]]
  (str-map format-tag tags))
