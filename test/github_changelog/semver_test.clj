(ns github-changelog.semver-test
  (:require [clojure.test :refer [are deftest is testing]]
            [github-changelog.semver :as sut]
            [github-changelog.spec :as spec]))

(deftest valid?
  (testing "with a string"
    (is (not (sut/valid? "1.0.0"))))
  (testing "with a valid version map"
    (is (sut/valid? {:major 1 :minor 0 :patch 0}))))

(deftest extract
  (testing "with 'v' prefix"
    (are [version] (sut/valid? (sut/extract version "v"))
      "v0.0.1"
      "v0.9.3-pre0"
      "v1.0.1"))
  (testing "without a prefix"
    (are [version] (sut/valid? (sut/extract version "v"))
      "0.0.1"
      "0.9.3-pre0"
      "1.0.1"))
  (testing "with 'pre' prefix"
    (are [version] (sut/valid? (sut/extract version "pre"))
      "pre0.0.1"
      "pre0.9.3-pre0"
      "pre1.0.1"))
  (testing "invalid tags"
    (are [version] (nil? (sut/extract version "v"))
      "something"
      "foobar"
      "versions")))

(deftest newer?
  (let [version (spec/sample ::sut/version)
        high    (assoc version :major 10)
        low     (assoc version :major 9)]
    (is (sut/newer? high low))
    (is (not (sut/newer? low high)))))

(deftest semver-type
  (are [type version] (= type (sut/get-type version))
    :major {:major 5 :minor 0 :patch 0 :pre-release nil :build nil}
    :minor {:major 5 :minor 1 :patch 0 :pre-release nil :build nil}
    :patch {:major 5 :minor 1 :patch 1 :pre-release nil :build nil}
    :pre-release {:major 5 :minor 1 :patch 1 :pre-release "pre" :build nil}
    :build {:major 5 :minor 1 :patch 1 :pre-release "pre" :build "build"}))

(deftest parse
  (are [version str-version] (= version (sut/parse str-version))
    {:major 1 :minor 0 :patch 0 :pre-release nil :build nil}          "1.0.0"
    {:major 1 :minor 1 :patch 0 :pre-release nil :build nil}          "1.1.0"
    {:major 1 :minor 1 :patch 1 :pre-release nil :build nil}          "1.1.1"
    {:major 1 :minor 1 :patch 1 :pre-release "pre0" :build nil}       "1.1.1-pre0"
    {:major 1 :minor 1 :patch 1 :pre-release "pre0" :build "build-1"} "1.1.1-pre0+build-1"))
