(ns github-changelog.markdown-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [github-changelog.markdown :as sut]))

(deftest h1
  (is (= "\n\n# Header\n\n" (sut/h1 "Header"))))

(deftest h2
  (is (= "\n\n## SubHeader\n\n" (sut/h2 "SubHeader"))))

(deftest link
  (is (= "[foo bar](http://example.com/)" (sut/link "foo bar" "http://example.com/")))
  (is (= "[http://example.com/](http://example.com/)" (sut/link "http://example.com/"))))

(deftest emphasis
  (is (= "**foo bar**" (sut/emphasis "foo bar"))))

(deftest ul
  (is (= "\n* A\n* B\n* C" (str/join (map sut/li ["A" "B" "C"])))))
