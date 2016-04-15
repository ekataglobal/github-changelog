(ns github-changelog.git
  (:require
   [github-changelog.util :refer [strip-trailing]]
   [clojure.string :as string]
   [clj-jgit.porcelain :as git]
   [clj-jgit.util :refer [name-from-uri]])
  (:import (java.io FileNotFoundException)
           (org.eclipse.jgit.api Git)
           (org.eclipse.jgit.lib Repository Ref)
           (org.eclipse.jgit.revwalk RevCommit)))

(defn gen-url [{:keys [git user repo]}]
  (format "%s/%s/%s.git" (strip-trailing git) user repo))

(def git-path name-from-uri)

(defn- clone-or-load [uri dir]
  (try
    (git/load-repo dir)
    (catch FileNotFoundException _
      (git/git-clone uri dir))))

(defn- refresh [^Repository repo]
  (git/git-fetch-all repo)
  repo)

(defn clone [{:keys [git-url dir update?]
              :or   {git-url (gen-url config)
                     dir     (git-path git-url)
                     update? true}
              :as config}]
  (cond-> (clone-or-load git-url dir)
    update? refresh))

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
