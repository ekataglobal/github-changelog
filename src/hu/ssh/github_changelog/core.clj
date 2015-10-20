(ns hu.ssh.github-changelog.core
  (:require
    [hu.ssh.github-changelog.util :as util]
    [hu.ssh.github-changelog.git :as git]
    [hu.ssh.github-changelog.github :as github]
    [environ.core :refer [env]]))

(defn- find-pull
  [pulls sha]
  {:pre  [(seq? pulls) (string? sha)]
   :post [(or (map? %) (nil? %))]}
  (first (filter #(= (:sha %) sha) pulls)))

(defn- assoc-pulls [pulls tag]
  {:pre [(map? tag) (seq? pulls)]}
  (let [related-pulls (->> (:commits tag)
                           (map (partial find-pull pulls))
                           (remove nil?))]
    (assoc tag :pulls related-pulls)))

(defn- assoc-ranges [tags]
  {:pre [(seq? tags)]}
  (let [previous-shas (concat (map :sha (rest tags)) [nil])]
    (map #(assoc %1 :from %2) tags previous-shas)))

(defn- assoc-commits [git tag]
  {:pre  [(map? tag)]
   :post [(:commits %)]}
  (assoc tag :commits (git/commits git (:from tag) (:sha tag))))

(defn- load-tags [user repo]
  {:pre  [(string? user) (string? repo)]
   :post [(seq? %)]}
  (let [git (git/clone (util/git-url prefix user repo))
        tags (git/version-tags git)]
    (map (partial assoc-commits git) (assoc-ranges tags))))

(def prefix "https://github.com")

(defn changelog
  "Fetches the changelog"
  [user repo {:keys [token]}]
  {:pre [(every? string? [user repo token])]}
  (let [pulls (github/fetch-pulls user repo {:token token})]
    (->> (load-tags user repo)
         (map (partial assoc-pulls pulls))
         (println))))
