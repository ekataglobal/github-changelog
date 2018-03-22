(ns github-changelog.core
  (:require [github-changelog
             [conventional :as conventional]
             [git :as git]
             [github :as github]
             [semver :as semver]]))

(defn assoc-semver [prefix {:keys [name] :as tag}]
  (assoc tag :version (semver/extract name prefix)))

(defn assoc-ranges [tags]
  (let [previous-shas (concat (map :sha (rest tags)) [nil])]
    (map #(assoc %1 :from %2) tags previous-shas)))

(defn parse-tags [tags prefix]
  (->> (map (partial assoc-semver prefix) tags)
       (filter :version)
       (sort-by :version semver/newer?)
       (assoc-ranges)))

(defn assoc-commits [git {:keys [from sha] :as tag}]
  (assoc tag :commits (git/commits git from sha)))

(defn load-tags [config]
  (let [git-repo (git/clone config)
        tags     (git/tags git-repo)
        prefix   (get config :tag-prefix "v")]
    (map (partial assoc-commits git-repo) (parse-tags tags prefix))))

(defn find-pull [pulls sha]
  (first (filter #(= (:sha %) sha) pulls)))

(defn assoc-pulls [pulls {:keys [commits] :as tag}]
  (let [related-pulls (remove nil? (map (partial find-pull pulls) commits))]
    (assoc tag :pulls related-pulls)))

(defn changelog
  "Fetches the changelog"
  [config]
  (->> (load-tags config)
       (map (partial assoc-pulls (github/fetch-pulls config)))
       (map (partial conventional/parse-changes config))))

(defn filter-tags [tags {:keys [last since]}]
  (cond->> tags
    since (filter #(semver/newer? (:version %) since))
    last  (take last)))
