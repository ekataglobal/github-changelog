(ns hu.ssh.github-changelog
  (:require
    [environ.core :refer [env]]
    [tentacles.core :as core]
    [tentacles.repos :as repos]
    [tentacles.pulls :as pulls]
    [clj-semver.core :as semver]))

(defn repo
  "Gets the repository from its name"
  [name]
  (vector "pro" name))

(defn parse-semver
  "Checks for semantic versions with or without v predicate"
  [tag]
  (let [version (:name tag)
        parse #(try (semver/parse %)
                    (catch java.lang.AssertionError _e nil))]
    (if (= \v (first version))
      (parse (apply str (rest version)))
      (parse version))))

(defn changelog
  "Fetches the changelog"
  [user repo]
  (let [tags (delay (map #(assoc % :version (parse-semver %)) (repos/tags user repo)))
        pulls (delay (pulls/pulls user repo {:state "closed"}))
        commits (delay (repos/commits user repo))]
    (println (first @tags))))

(core/with-defaults {:oauth-token (env :github-token) :all_pages true}
                    (changelog "raszi" "node-tmp"))
