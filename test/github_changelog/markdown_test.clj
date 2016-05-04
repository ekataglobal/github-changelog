(ns github-changelog.markdown-test
  (:require [clojure
             [string :refer [join]]
             [test :refer :all]]
            [github-changelog.markdown :as markdown]))

(deftest h1
  (is (= "\n\n# Header\n\n" (markdown/h1 "Header"))))

(deftest h2
  (is (= "\n\n## SubHeader\n\n" (markdown/h2 "SubHeader"))))

(deftest link
  (is (= "[foo bar](http://example.com/)" (markdown/link "foo bar" "http://example.com/")))
  (is (= "[http://example.com/](http://example.com/)" (markdown/link "http://example.com/"))))

(deftest emphasis
  (is (= "**foo bar**" (markdown/emphasis "foo bar"))))

(deftest ul
  (is (= "\n* A\n* B\n* C" (join (map markdown/li ["A" "B" "C"])))))
