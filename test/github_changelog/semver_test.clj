(ns github-changelog.semver-test
  (:require
    [github-changelog.semver :as semver]
    [github-changelog.schema :refer [Semver]]
    [github-changelog.schema-complete :refer [complete]]
    [github-changelog.version-examples :refer :all]
    [clojure.test :refer :all]
    [schema.core :as s]))

(deftest extract
  (testing "with a v prefix"
    (are [version] (s/validate Semver (semver/extract version))
                   "v0.0.1"
                   "v0.9.3-pre0"
                   "v1.0.1"))
  (testing "without a v prefix"
    (are [version] (s/validate Semver (semver/extract version))
                   "0.0.1"
                   "0.9.3-pre0"
                   "1.0.1"))
  (testing "invalid tags"
    (are [version] (nil? (semver/extract version))
                   "something"
                   "foobar"
                   "versions")))

(deftest newer?
  (let [high (complete {:major 1} Semver)
        low (complete {:major 0} Semver)]
    (is (semver/newer? high low))
    (is (not (semver/newer? low high)))))

(deftest semver-type
  (are [type version] (= type (semver/get-type version))
                      :major v-major
                      :minor v-minor
                      :patch v-patch
                      :pre-release v-pre-release
                      :build v-build))
