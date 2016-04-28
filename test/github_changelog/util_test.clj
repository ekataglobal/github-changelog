(ns github-changelog.util-test
  (:require [clojure.test :refer :all]
            [github-changelog.util :as util]))

(deftest strip-trailing
  (is (= "something" (util/strip-trailing "something///")))
  (is (= "foobar" (util/strip-trailing "foobar" "!!"))))

(deftest extract-params
  (is (= {} (util/extract-params "")))
  (is (= {:a "1" :b "2"} (util/extract-params "a=1&b=2"))))
