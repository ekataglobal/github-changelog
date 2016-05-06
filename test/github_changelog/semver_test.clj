(ns github-changelog.semver-test
  (:require [clj-semver.core :as clj-semver]
            [clojure.test :refer :all]
            [github-changelog
             [schema-generators :as sgen]
             [semver :as semver]
             [version-examples :refer :all]]))

(deftest extract
  (testing "with a v prefix"
    (are [version] (clj-semver/valid? (semver/extract version))
      "v0.0.1"
      "v0.9.3-pre0"
      "v1.0.1"))
  (testing "without a v prefix"
    (are [version] (clj-semver/valid? (semver/extract version))
      "0.0.1"
      "0.9.3-pre0"
      "1.0.1"))
  (testing "invalid tags"
    (are [version] (nil? (semver/extract version))
      "something"
      "foobar"
      "versions")))

(deftest newer?
  (let [high (sgen/complete-semver {:major 1})
        low (sgen/complete-semver {:major 0})]
    (is (semver/newer? high low))
    (is (not (semver/newer? low high)))))

(deftest semver-type
  (are [type version] (= type (semver/get-type version))
    :major v-major
    :minor v-minor
    :patch v-patch
    :pre-release v-pre-release
    :build v-build))

(deftest parse
  (are [version str-version] (= version (semver/parse str-version))
    {:major 1 :minor 0 :patch 0 :pre-release nil :build nil}          "1.0.0"
    {:major 1 :minor 1 :patch 0 :pre-release nil :build nil}          "1.1.0"
    {:major 1 :minor 1 :patch 1 :pre-release nil :build nil}          "1.1.1"
    {:major 1 :minor 1 :patch 1 :pre-release "pre0" :build nil}       "1.1.1-pre0"
    {:major 1 :minor 1 :patch 1 :pre-release "pre0" :build "build-1"} "1.1.1-pre0+build-1"))
