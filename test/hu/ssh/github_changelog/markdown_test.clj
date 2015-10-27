(ns hu.ssh.github-changelog.markdown-test
  (:require
    [hu.ssh.github-changelog.markdown :as markdown]
    [clojure.test :refer :all]))

(deftest header
  (are [expected level body] (= expected (markdown/header level body))
                             "# Header" 1 "Header"
                             "## SubHeader" 2 "SubHeader"))

(deftest link
  (is (= "[foo bar](http://example.com/)" (markdown/link "foo bar" "http://example.com/")))
  (is (= "[http://example.com/](http://example.com/)" (markdown/link "http://example.com/"))))
