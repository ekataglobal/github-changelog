(ns github-changelog.git-helper
  (:require [clojure.java.shell :as shell]
            [github-changelog.fs :as fs])
  (:import java.util.UUID))

(defn init-repo []
  (let [dir  (fs/tmp-dir nil "github-changelog-repo_")
        file (fs/tmp-file dir)
        name (fs/basename file)]
    (shell/with-sh-dir dir
      (shell/sh "git" "init" ".")
      (shell/sh "git" "add" name)
      (shell/sh "git" "commit" "-m" "Initial commit"))
    [dir name]))

(defn add-file [repo]
  (let [file (fs/tmp-file repo)
        name (fs/basename file)]
    (shell/with-sh-dir repo
      (shell/sh "git" "add" name)
      (shell/sh "git" "commit" "-m" (format "%s addded" name)))
    name))

(defn add-tag
  ([repo] (add-tag repo (str (UUID/randomUUID))))
  ([repo tag]
   (shell/sh "git" "tag" tag :dir repo)
   tag))

(defn merge-orphan-branch [repo]
  (let [branch "orphan"]
    (shell/sh "git" "checkout" "--orphan" branch :dir repo)
    (add-file repo)
    (shell/sh "git" "checkout" "master" :dir repo)
    (shell/sh "git" "merge" "--allow-unrelated-histories" branch :dir repo)))
