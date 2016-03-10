(ns github-changelog.validators-test
  (:require
   [github-changelog.validators :as validators]
   [clojure.test :refer :all]))

(def min-length-validator (validators/min-length 2))

(deftest min-length
  (is (fn? (first min-length-validator)))
  (is (string? (second min-length-validator)))
  (are [expected input] (= expected ((first min-length-validator) input))
    false ""
    false "x"
    true "xx"))

(def url-validator (validators/url))

(deftest url
  (is (fn? (first url-validator)))
  (is (string? (second url-validator)))
  (are [expected input] (= expected ((first url-validator) input))
    false ""
    false "x"
    true "https://github.com/"))
