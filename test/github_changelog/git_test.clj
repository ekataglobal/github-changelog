(ns github-changelog.git-test
  (:require [clojure.test :refer :all]
            [github-changelog.git :as sut]))

(def config {:user "user" :repo "repo"})

(deftest gen-url
  (is (= "https://github.com/user/repo.git" (sut/gen-url config))))
