(ns github-changelog.git
  (:require
   [clojure.string :as string]
   [clj-jgit.porcelain :as git]
   [clj-jgit.util :refer [name-from-uri]])
  (:import (java.io FileNotFoundException)
           (org.eclipse.jgit.api Git)
           (org.eclipse.jgit.lib Repository Ref)
           (org.eclipse.jgit.revwalk RevCommit)))

(def git-path name-from-uri)

(defn- clone-or-load [uri dir]
  (try
    (git/load-repo dir)
    (catch FileNotFoundException _
      (git/git-clone uri dir))))

(defn clone [uri dir]
  (let [path (or dir (git-path uri))
        repo (clone-or-load uri path)]
    (git/git-fetch-all repo)
    repo))

(defn- get-merge-sha [^Repository repo ^Ref tag]
  (let [peeled (.peel repo tag)]
    (.name (if-let [peeled-id (.getPeeledObjectId peeled)] peeled-id (.getObjectId peeled)))))

(defn- map-tag-name [^Ref tag]
  (string/replace (.getName tag) #"^refs/tags/" ""))

(defn- map-tag [^Repository repo ^Ref tag]
  {:name (map-tag-name tag)
   :sha (get-merge-sha repo tag)})

(defn tags [^Git git]
  (let [repo (.getRepository git)
        tags (.. git tagList call)]
    (map (partial map-tag repo) tags)))

(defn- get-commit-sha [^RevCommit log]
  (.name log))

(defn commits [^Git git from until]
  (map get-commit-sha
       (if (nil? from)
         (git/git-log git until)
         (git/git-log git from until))))
