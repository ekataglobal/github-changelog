(ns github-changelog.core
  (:require [clojure.spec.alpha :as s]
            [github-changelog.config :as config]
            [github-changelog.conventional :as conventional]
            [github-changelog.core-spec :as core-spec]
            [github-changelog.git :as git]
            [github-changelog.github :as github]
            [github-changelog.semver :as semver]))

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

(defn assoc-commits [git-repo {:keys [from sha] :as tag}]
  (assoc tag :commits (git/commits git-repo from sha)))

(defn map-commits [tags git-repo]
  (map (partial assoc-commits git-repo) tags))

(defn ^:no-gen load-tags [config]
  (let [git-repo (git/init config)
        prefix   (get config :tag-prefix "v")]
    (-> (git/tags git-repo)
        (parse-tags prefix)
        (map-commits git-repo))))

(s/fdef load-tags
  :args (s/cat :config ::config/config-map)
  :ret (s/* ::core-spec/tag))

(defn find-pull [pulls sha]
  (first (filter #(= (github/get-sha %) sha) pulls)))

(defn assoc-pulls [pulls {:keys [commits] :as tag}]
  (->> commits
       (keep (partial find-pull pulls))
       (assoc tag :pulls)))

(s/fdef assoc-pulls
  :args (s/cat :pulls (s/coll-of ::github/pull) :tag ::core-spec/tag)
  :ret ::core-spec/tag-with-pulls)

(defn ^:no-gen collect-tags [config]
  (let [pulls (github/fetch-pulls config)]
    (->> (load-tags config)
         (map (partial assoc-pulls pulls)))))

(s/fdef collect-tags
  :args (s/cat :config ::config/config-map)
  :ret (s/* ::core-spec/tag-with-pulls))

(defn ^:no-gen changelog
  "Fetches the changelog"
  [config]
  (->> (collect-tags config)
       (map (partial conventional/parse-changes config))))

(s/fdef changelog
  :args (s/cat :config ::config/config-map)
  :ret (s/* ::conventional/tag-with-changes))

(defn filter-tags [tags {:keys [last since until]}]
  (cond->> tags
    since (filter #(semver/newer? (:version %) since))
    until (filter #(not (semver/newer? (:version %) until)))
    last  (take last)))
