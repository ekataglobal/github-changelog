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

(defn- fetch-pulls
  "Fetches the pull-requests"
  []
  (pulls/pulls *user* *repo* {:state "closed"}))

(defn- merge-sha
  "Gets the merge commit from a tag"
  [tag]
  (get-in tag [:commit :sha]))

(defn- commits-until
  [commits sha]
  (take-while #(not= (:sha %) sha) commits))

(defn- partition-commits [tags commits]
  (if (empty? commits)
    []
    (let [related-commits (commits-until commits (merge-sha (second tags)))]
      (cons related-commits (lazy-seq (partition-commits (rest tags) (drop (count related-commits) commits)))))))

(defn- fetch-commits
  [& {:keys [sha]}]
  (repos/commits *user* *repo* {:sha sha}))

(defn- map-commits
  "Maps commits into tags"
  [tags]
  (let [last-sha (merge-sha (first tags))
        all-commits (fetch-commits :sha last-sha)]
    (map #(assoc %1 :commits %2) tags (partition-commits tags all-commits))))

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
               (with-repo "raszi" "changelog-test" (changelog)))
