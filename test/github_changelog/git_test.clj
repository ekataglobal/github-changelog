(ns github-changelog.git-test
  (:require [clojure.java.shell :as shell]
            [clojure.test :refer :all]
            [github-changelog
             [fs :as fs]
             [git :as sut]]
            [clojure.string :as str])
  (:import java.util.UUID))

(def config {:user "user" :repo "repo"})

(defn- init-repo []
  (let [dir  (fs/tmp-dir nil "github-changelog-repo_")
        file (fs/tmp-file dir)
        name (fs/basename file)]
    (shell/with-sh-dir dir
      (shell/sh "git" "init" ".")
      (shell/sh "git" "add" name)
      (shell/sh "git" "commit" "-m" "Initial commit"))
    [dir name]))

(deftest name-from-uri
  (are [expected uri] (= expected (sut/name-from-uri uri))
    "github-changelog" "git@github.com:whitepages/github-changelog.git"
    "github-changelog" "https://github.com/whitepages/github-changelog.git"
    "github-changelog" "https://github.com/whitepages/github-changelog"
    "github-changelog" "/whitepages/github-changelog"))

(deftest gen-url
  (is (= "https://github.com/user/repo.git" (sut/gen-url config))))

(deftest clone
  (let [[base file] (init-repo)
        work        (fs/tmp-dir nil "github-changelog-clone_")
        repo        (sut/clone base work)]
    (is (sut/git-dir? repo))
    (is (fs/file? (fs/as-file work file)))
    (fs/rm-dir base)
    (fs/rm-dir work)))

(deftest clone-or-load
  (testing "with an existing repo"
    (let [[base] (init-repo)]
      (sut/clone-or-load base base)
      (is (sut/git-dir? base))
      (fs/rm-dir base)))
  (testing "with a non-existing repo"
    (let [[base file] (init-repo)
          other       (fs/tmp-dir)]
      (sut/clone-or-load base other)
      (is (sut/git-dir? other))
      (is (fs/file? (fs/as-file other file)))
      (fs/rm-dir base)
      (fs/rm-dir other))))

(defn- add-file [repo]
  (let [file (fs/tmp-file repo)
        name (fs/basename file)]
    (shell/with-sh-dir repo
      (shell/sh "git" "add" name)
      (shell/sh "git" "commit" "-m" (format "%s addded" name)))
    name))

(deftest refresh
  (let [[base] (init-repo)
        other  (fs/tmp-dir nil "github-changelog-clone_")
        _      (sut/clone-or-load base other)
        name   (add-file base)]
    (is (not (fs/exists? (fs/as-file other name))))
    (sut/refresh other)
    (is (fs/exists? (fs/as-file other name)))
    (fs/rm-dir base)
    (fs/rm-dir other)))

(defn- add-tag [repo]
  (let [tag (str (UUID/randomUUID))]
    (shell/sh "git" "tag" tag :dir repo)
    tag))

(deftest tags
  (let [[repo] (init-repo)
        tag-fn #(count (sut/tags repo))]
    (is (zero? (tag-fn)))
    (add-tag repo)
    (is (= 1 (tag-fn)))
    (add-tag repo)
    (is (= 2 (tag-fn)))))

(deftest commits
  (let [[repo]    (init-repo)
        initial   (str/trim (:out (shell/sh "git" "rev-list" "--max-parents=0" "HEAD" :dir repo)))
        commit-fn #(count (sut/commits repo initial nil))]
    (is (zero? (commit-fn)))
    (add-file repo)
    (is (= 1 (commit-fn)))))
