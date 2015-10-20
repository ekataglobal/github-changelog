(ns hu.ssh.github-changelog.git
  (:require
    [hu.ssh.github-changelog.semver :as semver]
    [clojure.string :as string]
    [clj-jgit.porcelain :as git]
    [clj-jgit.util :refer [name-from-uri]])
  (:import (java.io FileNotFoundException)
           (org.eclipse.jgit.api Git)
           (org.eclipse.jgit.lib Repository Ref)
           (org.eclipse.jgit.revwalk RevCommit)))

(defn- git? [x] (instance? Git x))
(defn- repo? [x] (instance? Repository x))
(defn- ref? [x] (instance? Ref x))
(defn- commit? [x] (instance? RevCommit x))

(defn- git-path [uri]
  {:pre [(string? uri)]}
  (name-from-uri uri))

(defn- clone-or-load [uri]
  {:pre  [(string? uri)]
   :post [(git? %)]}
  (let [path (git-path uri)]
    (try
      (git/load-repo path)
      (catch FileNotFoundException _
        (git/git-clone uri path)))))

(defn clone [uri]
  {:pre  [(string? uri)]
   :post [(git? %)]}
  (let [repo (clone-or-load uri)]
    (git/git-fetch-all repo)
    repo))

(defn- get-merge-sha [repo tag]
  {:pre  [(repo? repo) (ref? tag)]
   :post [(string? %)]}
  (let [peeled (.peel repo tag)]
    (.name (if-let [peeled-id (.getPeeledObjectId peeled)] peeled-id (.getObjectId peeled)))))

(defn- map-tag-name [tag]
  {:pre [(ref? tag)]}
  (string/replace (.getName tag) #"^refs/tags/", ""))

(defn- map-tag [repo tag]
  {:pre  [(repo? repo)]
   :post [(map? %)]}
  {:name (map-tag-name tag) :sha (get-merge-sha repo tag)})

(defn- assoc-semver [tag]
  {:pre  [(map? tag) (:name tag)]
   :post [(contains? % :version)]}
  (assoc tag :version (semver/extract (:name tag))))

(defn tags [git]
  {:pre  [(git? git)]
   :post [(every? map? %)]}
  (let [repo (.getRepository git)
        tags (.. git tagList call)]
    (map (partial map-tag repo) tags)))

(defn version-tags [git]
  {:pre  [(git? git)]
   :post [(every? map? %)]}
  (->> (tags git)
       (map assoc-semver)
       (filter :version)
       (sort-by :version semver/newer?)))

(defn- get-commit-sha [log]
  {:pre  [(commit? log)]
   :post [(string? %)]}
  (.name log))

(defn commits [git from until]
  {:pre  [(git? git) (string? until) (or (nil? from) (string? from))]
   :post [(seq? %)]}
   (map get-commit-sha (if (nil? from) (git/git-log git until) (git/git-log git from until))))
