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
                   "1.0.1")))

(deftest newer?
  (let [high (complete {:major 1} Semver)
        low (complete {:major 0} Semver)]
    (is (semver/newer? high low))
    (is (not (semver/newer? low high)))))

(deftest semver-type
  (is (= (semver/get-type v-major) :major))
  (is (= (semver/get-type v-minor) :minor))
  (is (= (semver/get-type v-patch) :patch))
  (is (= (semver/get-type v-pre-release) :pre-release))
  (is (= (semver/get-type v-build) :build)))
