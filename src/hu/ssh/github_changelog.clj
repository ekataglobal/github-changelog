(ns hu.ssh.github-changelog
  (:require
    [environ.core :refer [env]]
    [tentacles.core :refer [with-defaults]]
    [tentacles.repos :as repos]
    [tentacles.pulls :as pulls]
    [clj-semver.core :as semver]))

(def ^:dynamic *user* "raszi")
(def ^:dynamic *repo* "changelog-test")

(defmacro with-repo [new-user new-repo & body]
  `(binding [*user* ~new-user *repo* ~new-repo]
     ~@body))

(defn- extract-semver
  "Extracts semantic versions with or without 'v' predicate from the tags"
  [tag]
  (let [version (:name tag)
        parse #(try (semver/parse %)
                    (catch java.lang.AssertionError _e nil))]
    (parse
      (if (= \v (first version))
        (subs version 1)
        version))))

(defn- map-semver
  "Maps semver to the tag"
  [tags]
  (map #(assoc % :version (extract-semver %)) tags))

(defn- fetch-version-tags
  "Fetch the version tags in the correct order"
  []
  (let [sort-fn #(semver/newer? (:version %1) (:version %2))]
    (->> (repos/tags *user* *repo*)
         map-semver
         (filter :version)
         (sort sort-fn))))

(defn- fetch-commits
  "Fetches commits for a tag"
  [tag]
  (let [sha (get-in tag [:commit :sha])]
    (repos/commits *user* *repo* {:sha sha})))

(defn- fetch-pulls
  "Fetches the pull-requests"
  []
  (pulls/pulls *user* *repo* {:state "closed"}))

(defn- map-commits
  "Maps commits into tags"
  [tags]
  (map #(assoc % :commits (fetch-commits %)) tags))

(defn- map-pulls
  "Maps pull-pull-requests to tags"
  [tags]
  (let [pulls (fetch-pulls)]
       tags))

(defn changelog
  "Fetches the changelog"
  []
  (->> (fetch-version-tags)
       map-commits
       map-pulls))

(with-defaults {:oauth-token (env :github-token) :all_pages true}
                    (with-repo "raszi" "changelog-test" (println (changelog))))
