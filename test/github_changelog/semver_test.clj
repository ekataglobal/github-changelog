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
