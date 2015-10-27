(ns hu.ssh.github-changelog.semver-test
  (:require
    [hu.ssh.github-changelog.semver :as semver]
    [hu.ssh.github-changelog.schema :refer [Semver]]
    [schema.experimental.generators :as g]
    [schema.experimental.complete :as c]
    [clojure.test :refer :all]))

(deftest extract
  (testing "with a v prefix"
    (is (semver/extract "v0.0.1"))
    (is (semver/extract "v0.9.3-pre0"))
    (is (semver/extract "v1.0.1")))
  (testing "without a v prefix"
    (is (semver/extract "0.0.1"))
    (is (semver/extract "0.9.3-pre0"))
    (is (semver/extract "1.0.1"))))

(deftest newer?
  (is (semver/newer? (c/complete {:major 1} Semver) (c/complete {:major 0} Semver))))
