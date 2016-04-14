(ns github-changelog.git-test
  (:require [github-changelog.git :as git]
            [clojure.test :refer :all]))

(def config {:git "https://github.com/":user "user" :repo "repo"})

(deftest gen-url
  (is (= "https://github.com/user/repo.git" (git/gen-url config))))
