(ns hu.ssh.github-changelog.semver-test
  (:require
    [hu.ssh.github-changelog.semver :as semver]
    [hu.ssh.github-changelog.schema :refer [Semver]]
    [schema.experimental.generators :as g]
    [schema.experimental.complete :as c]
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
  (let [high (c/complete {:major 1} Semver)
        low (c/complete {:major 0} Semver)]
    (is (semver/newer? high low))
    (is (not (semver/newer? low high)))))
