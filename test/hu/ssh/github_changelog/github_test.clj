(ns hu.ssh.github-changelog.github-test
  (:require
    [hu.ssh.github-changelog.github :as github]
    [hu.ssh.github-changelog.schema :refer [Pull]]
    [hu.ssh.github-changelog.util :refer [gen-sha]]
    [clojure.test :refer :all]
    [schema.core :as s]))

(deftest parse-pull
  (let [sha (gen-sha)
        pull (github/parse-pull {:title "Something" :body nil :head {:sha sha}})]
    (is (s/validate Pull pull))
    (is (= sha (:sha pull)))))
