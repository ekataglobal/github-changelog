(ns hu.ssh.github-changelog.markdown-test
  (:require
    [hu.ssh.github-changelog.markdown :as markdown]
    [clojure.test :refer :all]))

(deftest header
  (are [expected level body] (= expected (markdown/header level body))
                             "# Header" 1 "Header"
                             "## SubHeader" 2 "SubHeader"))
