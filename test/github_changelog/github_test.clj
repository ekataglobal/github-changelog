(ns github-changelog.github-test
  (:require
    [github-changelog.github :as github]
    [github-changelog.schema :refer [Config Pull]]
    [github-changelog.util :refer [gen-sha]]
    [clojure.test :refer :all]
    [schema.core :as s]
    [schema.experimental.complete :as c]))

(def github-api "https://api.github.com/")
(def config (c/complete {:github-api github-api :user "raszi" :repo "changelog-test"} Config))

(deftest pulls-url
  (is (= "https://api.github.com/repos/raszi/changelog-test/pulls" (github/pulls-url config))))

(deftest parse-pull
  (let [sha (gen-sha)
        pull (github/parse-pull {:title "Something" :body nil :head {:sha sha}})]
    (is (s/validate Pull pull))
    (is (= sha (:sha pull)))))
