(ns github-changelog.git-test
  (:require [clojure.test :refer :all]
            [github-changelog.git :as git]))

(def config {:user "user" :repo "repo"})

(deftest gen-url
  (is (= "https://github.com/user/repo.git" (git/gen-url config))))
