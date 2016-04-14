(ns github-changelog.git-test
  (:require [github-changelog.git :as git]
            [clojure.test :refer :all]))

(def config {:git "https://github.com/":user "user" :repo "repo"})

(deftest git-url
  (is (= "https://github.com/user/repo.git" (git/git-url config))))
