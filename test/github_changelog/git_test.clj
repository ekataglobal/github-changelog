(ns github-changelog.git-test
  (:require [clojure.test :refer [are deftest is testing]]
            [github-changelog.fs :as fs]
            [github-changelog.git :as sut]
            [github-changelog.git-helper :as gh]))

(def config {:user "user" :repo "repo"})

(deftest name-from-uri
  (are [expected uri] (= expected (sut/name-from-uri uri))
    "github-changelog" "git@github.com:whitepages/github-changelog.git"
    "github-changelog" "https://github.com/whitepages/github-changelog.git"
    "github-changelog" "https://github.com/whitepages/github-changelog"
    "github-changelog" "/whitepages/github-changelog"))

(deftest gen-url
  (is (= "https://github.com/user/repo.git" (sut/gen-url config))))

(deftest git-dir?
  (let [[git-dir] (gh/init-repo)
        tmp-dir   (fs/tmp-dir)]
    (try
      (is (sut/git-dir? git-dir))
      (is (not (sut/git-dir? tmp-dir)))
      (finally
        (fs/rm-dir git-dir)
        (fs/rm-dir tmp-dir)))))

(deftest clone
  (let [[base file] (gh/init-repo)
        work        (fs/tmp-dir nil "github-changelog-clone_")
        repo        (sut/clone base work)]
    (try
      (is (sut/git-dir? repo))
      (is (fs/file? (fs/as-file work file)))
      (finally
        (fs/rm-dir base)
        (fs/rm-dir work)))))

(deftest clone-or-load
  (testing "with an existing repo"
    (let [[base] (gh/init-repo)]
      (try
        (sut/clone-or-load base base)
        (is (sut/git-dir? base))
        (finally
          (fs/rm-dir base)))))
  (testing "with a non-existing repo"
    (let [[base file] (gh/init-repo)
          other       (fs/tmp-dir)]
      (try
        (sut/clone-or-load base other)
        (is (sut/git-dir? other))
        (is (fs/file? (fs/as-file other file)))
        (finally
          (fs/rm-dir base)
          (fs/rm-dir other))))))

(deftest refresh
  (let [[base] (gh/init-repo)
        other  (fs/tmp-dir nil "github-changelog-clone_")
        _      (sut/clone-or-load base other)
        name   (gh/add-file base)]
    (try
      (is (not (fs/exists? (fs/as-file other name))))
      (sut/refresh other)
      (is (fs/exists? (fs/as-file other name)))
      (finally
        (fs/rm-dir base)
        (fs/rm-dir other)))))

(deftest tags
  (let [[repo] (gh/init-repo)
        tag-fn #(count (sut/tags repo))]
    (try
      (is (zero? (tag-fn)))
      (gh/add-tag repo)
      (is (= 1 (tag-fn)))
      (gh/add-tag repo)
      (is (= 2 (tag-fn)))
      (finally
        (fs/rm-dir repo)))))

(deftest initial-commit
  (let [single-initial-commit? #(= 40 (count (sut/initial-commit %)))]
    (testing "single initial commit"
      (let [[repo] (gh/init-repo)]
        (try
          (is (single-initial-commit? repo))
        (finally
          (fs/rm-dir repo)))))
    (testing "multiple initial commits present in the repository"
      (let [[repo] (gh/init-repo)]
        (try
          (gh/merge-orphan-branch repo)
          (is (single-initial-commit? repo))
        (finally
          (fs/rm-dir repo)))))))

(deftest commits
  (let [[repo]    (gh/init-repo)
        commit-fn #(count (sut/commits repo nil nil))]
    (try
      (is (zero? (commit-fn)))
      (gh/add-file repo)
      (is (= 1 (commit-fn)))
      (finally
        (fs/rm-dir repo)))))
