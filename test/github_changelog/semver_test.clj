(ns github-changelog.semver-test
  (:require [clj-semver.core :as semver]
            [clojure.test :refer :all]
            [github-changelog
             [schema-generators :as sgen]
             [semver :as sut]
             [version-examples :as v]]))

(deftest extract
  (testing "with 'v' prefix"
    (are [version] (semver/valid? (sut/extract version "v"))
      "v0.0.1"
      "v0.9.3-pre0"
      "v1.0.1"))
  (testing "without a prefix"
    (are [version] (semver/valid? (sut/extract version "v"))
      "0.0.1"
      "0.9.3-pre0"
      "1.0.1"))
  (testing "with 'pre' prefix"
    (are [version] (semver/valid? (sut/extract version "pre"))
      "pre0.0.1"
      "pre0.9.3-pre0"
      "pre1.0.1"))
  (testing "invalid tags"
    (are [version] (nil? (sut/extract version "v"))
      "something"
      "foobar"
      "versions")))

(deftest newer?
  (let [high (sgen/complete-semver {:major 1})
        low (sgen/complete-semver {:major 0})]
    (is (sut/newer? high low))
    (is (not (sut/newer? low high)))))

(deftest semver-type
  (are [type version] (= type (sut/get-type version))
    :major v/major
    :minor v/minor
    :patch v/patch
    :pre-release v/pre-release
    :build v/build))

(deftest parse
  (are [version str-version] (= version (sut/parse str-version))
    {:major 1 :minor 0 :patch 0 :pre-release nil :build nil}          "1.0.0"
    {:major 1 :minor 1 :patch 0 :pre-release nil :build nil}          "1.1.0"
    {:major 1 :minor 1 :patch 1 :pre-release nil :build nil}          "1.1.1"
    {:major 1 :minor 1 :patch 1 :pre-release "pre0" :build nil}       "1.1.1-pre0"
    {:major 1 :minor 1 :patch 1 :pre-release "pre0" :build "build-1"} "1.1.1-pre0+build-1"))
