(ns github-changelog.git-test
  (:require [clojure
             [string :as str]
             [test :refer :all]]
            [clojure.java.shell :as shell]
            [github-changelog
             [fs :as fs]
             [git :as sut]
             [git-helper :as gh]]))

(def config {:user "user" :repo "repo"})

(deftest name-from-uri
  (are [expected uri] (= expected (sut/name-from-uri uri))
    "github-changelog" "git@github.com:whitepages/github-changelog.git"
    "github-changelog" "https://github.com/whitepages/github-changelog.git"
    "github-changelog" "https://github.com/whitepages/github-changelog"
    "github-changelog" "/whitepages/github-changelog"))

(deftest gen-url
  (is (= "https://github.com/user/repo.git" (sut/gen-url config))))

(deftest clone
  (let [[base file] (gh/init-repo)
        work        (fs/tmp-dir nil "github-changelog-clone_")
        repo        (sut/clone base work)]
    (is (sut/git-dir? repo))
    (is (fs/file? (fs/as-file work file)))
    (fs/rm-dir base)
    (fs/rm-dir work)))

(deftest clone-or-load
  (testing "with an existing repo"
    (let [[base] (gh/init-repo)]
      (sut/clone-or-load base base)
      (is (sut/git-dir? base))
      (fs/rm-dir base)))
  (testing "with a non-existing repo"
    (let [[base file] (gh/init-repo)
          other       (fs/tmp-dir)]
      (sut/clone-or-load base other)
      (is (sut/git-dir? other))
      (is (fs/file? (fs/as-file other file)))
      (fs/rm-dir base)
      (fs/rm-dir other))))

(deftest refresh
  (let [[base] (gh/init-repo)
        other  (fs/tmp-dir nil "github-changelog-clone_")
        _      (sut/clone-or-load base other)
        name   (gh/add-file base)]
    (is (not (fs/exists? (fs/as-file other name))))
    (sut/refresh other)
    (is (fs/exists? (fs/as-file other name)))
    (fs/rm-dir base)
    (fs/rm-dir other)))

(deftest tags
  (let [[repo] (gh/init-repo)
        tag-fn #(count (sut/tags repo))]
    (is (zero? (tag-fn)))
    (gh/add-tag repo)
    (is (= 1 (tag-fn)))
    (gh/add-tag repo)
    (is (= 2 (tag-fn)))))

(deftest commits
  (let [[repo]    (gh/init-repo)
        commit-fn #(count (sut/commits repo nil nil))]
    (is (zero? (commit-fn)))
    (gh/add-file repo)
    (is (= 1 (commit-fn)))))
