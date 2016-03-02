(ns github-changelog.git
  (:require
    [clojure.string :as string]
    [github-changelog.schema :refer [Sha Tag]]
    [clj-jgit.porcelain :as git]
    [clj-jgit.util :refer [name-from-uri]]
    [schema.core :as s])
  (:import (java.io FileNotFoundException)
           (org.eclipse.jgit.api Git)
           (org.eclipse.jgit.lib Repository Ref)
           (org.eclipse.jgit.revwalk RevCommit)))

(s/set-fn-validation! true)

(s/defn git-path :- s/Str
  [uri :- s/Str]
  (name-from-uri uri))

(s/defn clone-or-load :- Git
  [uri :- s/Str]
  (let [path (git-path uri)]
    (try
      (git/load-repo path)
      (catch FileNotFoundException _
        (git/git-clone uri path)))))

(s/defn clone :- Git
  [uri :- s/Str]
  (let [repo (clone-or-load uri)]
    (git/git-fetch-all repo)
    repo))

(s/defn get-merge-sha :- Sha
  [repo :- Repository
   tag :- Ref]
  (let [peeled (.peel repo tag)]
    (.name (if-let [peeled-id (.getPeeledObjectId peeled)] peeled-id (.getObjectId peeled)))))

(s/defn map-tag-name :- s/Str
  [tag :- Ref]
  (string/replace (.getName tag) #"^refs/tags/", ""))

(s/defn map-tag :- Tag
  [repo :- Repository
   tag :- Ref]
  {:name (map-tag-name tag) :sha (get-merge-sha repo tag)})

(s/defn tags :- [Tag]
  [git :- Git]
  (let [repo (.getRepository git)
        tags (.. git tagList call)]
    (map (partial map-tag repo) tags)))

(s/defn get-commit-sha :- s/Str
  [log :- RevCommit]
  (.name log))

(s/defn commits :- [Sha]
  [git :- Git
   from :- (s/maybe Sha)
   until :- Sha]
   (map get-commit-sha (if (nil? from) (git/git-log git until) (git/git-log git from until))))
