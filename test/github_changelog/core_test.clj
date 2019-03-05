(ns github-changelog.core-test
  (:require [clojure.test :refer :all]
            [github-changelog.core :as sut]
            [github-changelog.git-helper :as gh]))

(deftest filter-tags
  (let [versions ["1.3.0" "1.2.0" "1.1.0" "1.0.0" "0.5.0" "0.2.0" "0.1.0"]
        tags     (map (partial hash-map :version) versions)]
    (are [expected options] (= expected (sut/filter-tags tags options))
      tags                                    {}
      [{:version "1.3.0"} {:version "1.2.0"}] {:last 2}
      [{:version "1.3.0"}]                    {:last 2 :since "1.2.0"}
      [{:version "1.2.0"} {:version "1.1.0"}] {:since "1.0.0" :until "1.2.0"})))

(deftest load-tags
  (let [[repo]  (gh/init-repo)
        config  {:user    "whitepages"
                 :repo    "github-changelog"
                 :dir     repo
                 :update? false}
        tags-fn #(count (sut/load-tags config))]
    (is (zero? (tags-fn)))
    (gh/add-tag repo "v0.0.1")
    (is (= 1 (tags-fn)))
    (gh/add-file repo)
    (gh/add-tag repo "v0.0.2")
    (is (= 2 (tags-fn)))))
