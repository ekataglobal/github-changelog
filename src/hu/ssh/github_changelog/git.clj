(ns hu.ssh.github-changelog.git
  (:require
    [clj-jgit.porcelain :as git]
    [clj-jgit.util :as util])
  (:import (java.io FileNotFoundException)))

(defn- repo? [x] (instance? org.eclipse.jgit.api.Git x))

(defn- git-path [uri]
  {:pre [(string? uri)]}
  (util/name-from-uri uri))

(defn- clone-or-load [uri]
  {:pre  [(string? uri)]
   :post [(repo? %)]}
  (let [path (git-path uri)]
    (try
      (git/load-repo path)
      (catch FileNotFoundException _
        (git/git-clone uri path)))))

(defn clone [uri]
  {:pre  [(string? uri)]
   :post [(repo? %)]}
  (let [repo (clone-or-load uri)]
    (git/git-fetch-all repo)
    repo))

(defn- get-merge-sha [tag]
  {:post [(string? %)]}
  (. (if-let [object-id (. tag getPeeledObjectId)] object-id (. tag getPeeledObjectId)) name))

(defn- map-tag [repo tag]
  {:post [(map? %)]}
  (let [peeled (. repo peel tag)]
    {:name (. tag getName) :sha (get-merge-sha peeled)}))

(defn tags [repo]
  {:pre  [(repo? repo)]
   :post [(every? map? %)]}
  (let [tags (.. repo tagList call)]
    (map (partial map-tag (. repo getRepository)) tags)))

; TODO (git/git-log repo (:sha (first taglist)) (:sha (second taglist)))
