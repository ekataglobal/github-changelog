(ns github-changelog.git
  (:require [clojure.java.shell :as shell]
            [clojure.string :as str]
            [github-changelog
             [defaults :as defaults]
             [fs :as fs]
             [util :as util]]))

(defn gen-url [{:keys [github user repo]
                :or   {github (:github defaults/config)}}]
  (format "%s/%s/%s.git" (util/strip-trailing github) user repo))

(defn name-from-uri [uri]
  (second (re-find #"/([^/]*?)(?:\.git)?$" uri)))

(defn exec [& args]
  (let [[_cmd opts] (split-with string? args)
        exit-codes  (get (apply hash-map opts) :exit-codes #{0})
        return      (apply shell/sh args)]
    (if (exit-codes (:exit return))
      return
      (throw (ex-info "Command execution failed" {:cmd args :return return})))))

(defn clone
  ([uri] (clone uri (name-from-uri uri)))
  ([uri dir]
   (exec "git" "clone" uri dir)
   dir))

(defn git-dir? [dir]
  (when (and (fs/dir? dir) (fs/dir? (fs/as-file dir ".git")))
    (let [exit (:exit (shell/sh "git" "status" :dir dir))]
      (zero? exit))))

(defn clone-or-load [uri dir]
  (if (git-dir? dir) dir (clone uri dir)))

(defn refresh [repo]
  (exec "git" "pull" "origin" :dir repo)
  repo)

(defn init [{:keys [git-url dir update?] :or {git-url (gen-url config)
                                              dir     (name-from-uri git-url)
                                              update? (:update? defaults/config)}
             :as   config}]
  (cond-> (clone-or-load git-url dir)
    update? refresh))

;; (defn- get-merge-sha [^Repository repo ^Ref tag]
;;   (let [peeled (.peel repo tag)]
;;     (.name (if-let [peeled-id (.getPeeledObjectId peeled)] peeled-id (.getObjectId peeled)))))

(defn- map-tag-name [tag]
  (str/replace tag #"^refs/tags/" ""))

(defn- map-tag [line]
  (let [[sha tag] (str/split line #" " 2)]
    {:name (map-tag-name tag)
     :sha  sha}))

(defn- split-lines [lines]
  (some-> lines str/trim not-empty str/split-lines))

(defn tags [dir]
  (->> (exec "git" "show-ref" "--tags" :exit-codes #{0 1} :dir dir)
       (:out)
       (split-lines)
       (map map-tag)))

(defn initial-commit [dir]
  (-> (exec "git" "rev-list" "--max-parents=0" "HEAD" :dir dir)
      :out
      str/trim))

(defn commits [dir from until]
  (let [commit (format "%s..%s"
                       (or from (initial-commit dir))
                       (or until "HEAD"))]
    (-> (exec "git" "rev-list" commit :dir dir)
        :out
        split-lines)))
