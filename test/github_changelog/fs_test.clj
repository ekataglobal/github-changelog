(ns github-changelog.fs-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [github-changelog.fs :as sut]))

(defn- file-tests [file]
  (is (string? file))
  (is (sut/exists? file))
  (is (sut/file? file)))

(deftest tmp-file
  (testing "without arguments"
    (let [file (sut/tmp-file)
          name (sut/basename file)]
      (file-tests file)
      (is (str/ends-with? name ".tmp"))
      (sut/rm file)))
  (testing "with prefix"
    (let [file (sut/tmp-file nil "prefix")
          name (sut/basename file)]
      (file-tests file)
      (is (str/starts-with? name "prefix"))
      (is (str/ends-with? name ".tmp"))
      (sut/rm file)))
  (testing "with prefix and postfix"
    (let [file (sut/tmp-file nil "prefix-" ".postfix")
          name (sut/basename file)]
      (file-tests file)
      (is (str/starts-with? name "prefix-"))
      (is (str/ends-with? name ".postfix"))
      (sut/rm file))))

(defn- dir-tests [dir]
  (is (string? dir))
  (is (sut/exists? dir))
  (is (sut/dir? dir)))

(deftest tmp-dir
  (testing "without arguments"
    (let [dir (sut/tmp-dir)]
      (try
        (dir-tests dir)
        (finally
          (sut/rm dir)))))
  (testing "with prefix"
    (let [dir  (sut/tmp-dir nil "github-changelog_")
          name (sut/basename dir)]
      (try
        (dir-tests dir)
        (is (str/starts-with? name "github-changelog_"))
        (finally
          (sut/rm dir))))))

(deftest dir?
  (is (sut/dir? (System/getProperty "java.io.tmpdir")))
  (is (not (sut/dir? (:file (meta #'dir?))))))

(deftest rm-dir
  (let [dir  (sut/tmp-dir)
        file (sut/tmp-file dir)]
    (dir-tests dir)
    (file-tests file)
    (sut/rm-dir dir)
    (is (not (sut/exists? file)))
    (is (not (sut/exists? dir)))))
