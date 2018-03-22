(ns github-changelog.util-test
  (:require [clojure.test :refer :all]
            [github-changelog.util :as sut]))

(deftest strip-trailing
  (is (= "something" (sut/strip-trailing "something///")))
  (is (= "foobar" (sut/strip-trailing "foobar" "!!"))))

(deftest extract-params
  (is (= {} (sut/extract-params "")))
  (is (= {:a "1" :b "2"} (sut/extract-params "a=1&b=2"))))
