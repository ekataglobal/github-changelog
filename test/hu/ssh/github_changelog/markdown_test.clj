(ns hu.ssh.github-changelog.markdown-test
  (:require
    [hu.ssh.github-changelog.markdown :as markdown]
    [clojure.test :refer :all]))

(deftest header
  (are [expected level body] (= expected (markdown/header level body))
                             "# Header\n" 1 "Header"
                             "## SubHeader\n" 2 "SubHeader"))

(deftest link
  (is (= "[foo bar](http://example.com/)" (markdown/link "foo bar" "http://example.com/")))
  (is (= "[http://example.com/](http://example.com/)" (markdown/link "http://example.com/"))))

(deftest emphasis
  (is (= "**foo bar**" (markdown/emphasis "foo bar"))))

(deftest ul
  (is (= "* a\n* b\n* c\n" (markdown/ul ["a" "b" "c"]))))
