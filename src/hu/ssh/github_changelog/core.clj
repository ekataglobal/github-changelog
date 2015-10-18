(ns hu.ssh.github-changelog.core
  (:require
    [hu.ssh.github-changelog.util :refer [value-at extract-semver]]
    [environ.core :refer [env]]
    [tentacles.core :refer [with-defaults with-url]]
    [tentacles.repos :as repos]
    [tentacles.pulls :as pulls]
    [clj-semver.core :refer [newer?]]))

(def ^:dynamic *user* "raszi")
;(def ^:dynamic *repo* "changelog-test")
(def ^:dynamic *repo* "node-tmp")
(def ^:dynamic *options* {})

(defmacro with-repo [new-user new-repo & body]
  `(binding [*user* ~new-user *repo* ~new-repo]
     ~@body))

(defmacro with-options [new-options & body]
  `(binding [*options* ~new-options]
     ~@body))

(defn- assoc-semver
  [tag]
  {:pre  [(map? tag) (:name tag)]
   :post [(contains? % :version)]}
  (assoc tag :version (extract-semver tag)))

(defn- fetch-version-tags
  "Fetch the version tags in the semver order"
  []
  (->> (repos/tags *user* *repo* *options*)
       (map assoc-semver)
       (filter :version)
       (sort-by :version newer?)))

(defn- fetch-pulls
  "Fetches the pull-requests"
  []
  (pulls/pulls *user* *repo* (merge *options* {:state "closed"})))

(def pull-sha (partial value-at [:head :sha]))
(def merge-sha (partial value-at [:commit :sha]))

(defn- commits-until
  [commits sha]
  {:pre [(seq? commits) (or (string? sha) (nil? sha))]}
  (println sha)
  (take-while #(not= (:sha %) sha) commits))

(defn- partition-commits [tags commits]
  (if (empty? commits)
    []
    (let [related-commits (commits-until commits (let [second-tag (second tags)] (if second-tag (merge-sha second-tag))))]
      (cons related-commits (lazy-seq (partition-commits (rest tags) (drop (count related-commits) commits)))))))

(defn- fetch-commits
  [& {:keys [sha]}]
  (repos/commits *user* *repo* (merge *options* {:sha sha})))

(defn- map-commits
  "Maps commits into tags"
  [tags]
  (let [latest-sha (merge-sha (first tags))
        all-commits (fetch-commits :sha latest-sha)]
    (map #(assoc %1 :commits %2) tags (partition-commits tags all-commits))))

(defn- find-pull
  [pulls sha]
  {:pre  [(seq? pulls) (string? sha)]
   :post [(or (map? %) (nil? %))]}
  (first (filter #(= (pull-sha %) sha) pulls)))

(defn- map-pulls
  "Maps pull-pull-requests to tags"
  [tags]
  (let [pulls (fetch-pulls)
        pulls-for #(->> (map :sha (:commits %))
                        (map (partial find-pull pulls))
                        (remove nil?))]
    (map #(assoc % :pulls (pulls-for %)) tags)))

(defn changelog
  "Fetches the changelog"
  [user repo {:keys [token]}]
  {:pre [(every? string? [user repo token])]}
  (->> (fetch-version-tags)
       map-commits
       map-pulls))

(def result (atom {}))

;(with-options {:oauth-token (env :oauth-token) :all-pages true}
;              (reset! result (changelog)))
