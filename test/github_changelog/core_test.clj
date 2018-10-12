(ns github-changelog.core-test
  (:require [clojure.test :refer :all]
            [github-changelog.core :as sut]))

(deftest filter-tags
  (let [versions ["1.3.0" "1.2.0" "1.1.0" "1.0.0" "0.5.0" "0.2.0" "0.1.0"]
        tags     (map (partial hash-map :version) versions)]
    (are [expected options] (= expected (sut/filter-tags tags options))
      tags                                    {}
      [{:version "1.3.0"} {:version "1.2.0"}] {:last 2}
      [{:version "1.3.0"}]                    {:last 2 :since "1.2.0"}
      [{:version "1.2.0"} {:version "1.1.0"}] {:since "1.0.0" :until "1.2.0"})))
