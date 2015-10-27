(ns hu.ssh.github-changelog.formatters.markdown
  (:require
    [hu.ssh.github-changelog.util :refer [str-map]]
    [hu.ssh.github-changelog.schema :refer [Tag Change ChangeType]]
    [hu.ssh.github-changelog.markdown :as markdown]
    [schema.core :as s]))

(s/defn format-change :- s/Str
  [change :- Change]
  (str (markdown/emphasis (:scope change))
       " "
       (:subject change)
       (if-let [issues (:issues change)]
         (str ", closes " (str-map #(markdown/link (first %) (second %)) issues)))))

(s/defn format-changes :- s/Str
  [[type changes :- [Change]]]
  (str (markdown/header 4 type)
       (markdown/ul (map format-change changes))))

(s/defn format-tag :- s/Str
  [tag :- Tag]
  (str (markdown/header 3 (:name tag))
       (str-map format-changes (group-by :type (:changes tag)))))

(s/defn format-tags :- s/Str
  "Generates a markdown version from the changes"
  [tags :- [Tag]]
  (str-map format-tag tags))
