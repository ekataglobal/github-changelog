(ns hu.ssh.github-changelog.core
  (:require
    [hu.ssh.github-changelog.util :as util]
    [hu.ssh.github-changelog.git :as git]
    [hu.ssh.github-changelog.semver :as semver]
    [hu.ssh.github-changelog.github :as github]
    [hu.ssh.github-changelog.conventional :as conventional]
    [environ.core :refer [env]]))

(defn- assoc-semver [tag]
  {:pre  [(map? tag) (:name tag)]
   :post [(contains? % :version)]}
  (assoc tag :version (semver/extract (:name tag))))

(defn- assoc-ranges [tags]
  {:pre [(seq? tags)]}
  (let [previous-shas (concat (map :sha (rest tags)) [nil])]
    (map #(assoc %1 :from %2) tags previous-shas)))

(defn parse-tags [tags]
  {:pre  [(seq? tags)]
   :post [(every? map? %)]}
  (->> (map assoc-semver tags)
       (filter :version)
       (sort-by :version semver/newer?)
       assoc-ranges))

(defn- assoc-commits [git tag]
  {:pre  [(map? tag)]
   :post [(:commits %)]}
  (assoc tag :commits (git/commits git (:from tag) (:sha tag))))

(defn- load-tags [config user repo]
  {:pre  [(map? config) (string? user) (string? repo)]
   :post [(seq? %)]}
  (let [prefix (:github config)
        git (git/clone (util/git-url prefix user repo))
        tags (git/tags git)]
    (map (partial assoc-commits git) (parse-tags tags))))

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

(defn changelog
  "Fetches the changelog"
  [user repo {:keys [token]}]
  {:pre [(every? string? [user repo token])]}
  (let [pulls (github/fetch-pulls user repo {:token token})
        config {:github "" :jira ""}]
    (->> (load-tags config user repo)
         (map (partial assoc-pulls pulls))
         (map (partial conventional/parse-changes config)))))
