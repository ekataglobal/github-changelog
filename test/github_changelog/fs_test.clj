(ns github-changelog.fs-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [github-changelog.fs :as sut]
            [github-changelog.test-fs :as test-fs]))

(defn- file-tests [file]
  (is (string? file))
  (is (test-fs/exists? file))
  (is (test-fs/file? file)))

(deftest tmp-file
  (testing "without arguments"
    (let [file (test-fs/tmp-file)
          name (test-fs/basename file)]
      (file-tests file)
      (is (str/ends-with? name ".tmp"))
      (test-fs/rm file)))
  (testing "with prefix"
    (let [file (test-fs/tmp-file nil "prefix")
          name (test-fs/basename file)]
      (file-tests file)
      (is (str/starts-with? name "prefix"))
      (is (str/ends-with? name ".tmp"))
      (test-fs/rm file)))
  (testing "with prefix and postfix"
    (let [file (test-fs/tmp-file nil "prefix-" ".postfix")
          name (test-fs/basename file)]
      (file-tests file)
      (is (str/starts-with? name "prefix-"))
      (is (str/ends-with? name ".postfix"))
      (test-fs/rm file))))

(defn- dir-tests [dir]
  (is (string? dir))
  (is (test-fs/exists? dir))
  (is (sut/dir? dir)))

(deftest tmp-dir
  (testing "without arguments"
    (let [dir (test-fs/tmp-dir)]
      (try
        (dir-tests dir)
        (finally
          (test-fs/rm dir)))))
  (testing "with prefix"
    (let [dir  (test-fs/tmp-dir nil "github-changelog_")
          name (test-fs/basename dir)]
      (try
        (dir-tests dir)
        (is (str/starts-with? name "github-changelog_"))
        (finally
          (test-fs/rm dir))))))

(deftest dir?
  (is (sut/dir? (System/getProperty "java.io.tmpdir")))
  (is (not (sut/dir? (:file (meta #'dir?))))))

(deftest rm-dir
  (let [dir  (test-fs/tmp-dir)
        file (test-fs/tmp-file dir)]
    (dir-tests dir)
    (file-tests file)
    (test-fs/rm-dir dir)
    (is (not (test-fs/exists? file)))
    (is (not (test-fs/exists? dir)))))
