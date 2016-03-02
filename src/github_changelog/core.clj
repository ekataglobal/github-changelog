(ns github-changelog.core
  (:require
    [github-changelog.util :refer [git-url]]
    [github-changelog.schema :refer [Config Tag Pull Sha Change]]
    [github-changelog.git :as git]
    [github-changelog.semver :as semver]
    [github-changelog.github :as github]
    [github-changelog.conventional :as conventional]
    [environ.core :refer [env]]
    [schema.core :as s])
  (:import (org.eclipse.jgit.api Git)))

(s/set-fn-validation! true)

(s/defn assoc-semver :- Tag
  [tag :- Tag]
  (assoc tag :version (semver/extract (:name tag))))

(s/defn assoc-ranges :- [Tag]
  [tags :- [Tag]]
  (let [previous-shas (concat (map :sha (rest tags)) [nil])]
    (map #(assoc %1 :from %2) tags previous-shas)))

(s/defn parse-tags :- [Tag]
  [tags :- [Tag]]
  (->> (map assoc-semver tags)
       (filter :version)
       (sort-by :version semver/newer?)
       assoc-ranges))

(s/defn assoc-commits :- Tag
  [git :- Git
   tag :- Tag]
  (assoc tag :commits (git/commits git (:from tag) (:sha tag))))

(s/defn load-tags :- [Tag]
  [config :- Config]
  (let [{:keys [user repo]} config
        prefix (:git config)
        git (git/clone (git-url prefix user repo))
        tags (git/tags git)]
    (map (partial assoc-commits git) (parse-tags tags))))

(s/defn find-pull :- (s/maybe Pull)
  [pulls :- [Pull]
   sha :- Sha]
  (first (filter #(= (:sha %) sha) pulls)))

(s/defn assoc-pulls :- Tag
  [pulls :- [Pull]
   tag :- Tag]
  (let [related-pulls (->> (:commits tag)
                           (map (partial find-pull pulls))
                           (remove nil?))]
    (assoc tag :pulls related-pulls)))

(s/defn changelog :- [Tag]
  "Fetches the changelog"
  [config :- Config]
  (let [pulls (github/fetch-pulls config)]
    (->> (load-tags config)
         (map (partial assoc-pulls pulls))
         (map (partial conventional/parse-changes config)))))
