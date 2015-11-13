(ns hu.ssh.github-changelog.markdown-test
  (:require
    [hu.ssh.github-changelog.markdown :as markdown]
    [clojure.test :refer :all]))

(deftest header
  (are [expected level body] (= expected (markdown/header level body))
                             "# Header\n\n" 1 "Header"
                             "## SubHeader\n\n" 2 "SubHeader"))

(deftest link
  (is (= "[foo bar](http://example.com/)" (markdown/link "foo bar" "http://example.com/")))
  (is (= "[http://example.com/](http://example.com/)" (markdown/link "http://example.com/"))))

(deftest emphasis
  (is (= "**foo bar**" (markdown/emphasis "foo bar"))))

(deftest ul
  (is (= "\n* A\n* B\n* C\n\n" (markdown/ul ["A" "B" "C"]))))
