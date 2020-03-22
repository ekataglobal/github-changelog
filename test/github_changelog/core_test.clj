(ns github-changelog.core-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test :refer [are deftest is]]
            [github-changelog.config :as config]
            [github-changelog.core :as sut]
            [github-changelog.core-spec :as core-spec]
            [github-changelog.git :as git]
            [github-changelog.git-helper :as gh]
            [github-changelog.github :as github]
            [github-changelog.semver :as semver]
            [github-changelog.spec :as spec]))

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

(deftest assoc-pulls
  (let [pulls (s/exercise ::github/pull)
        shas  (map github/get-sha pulls)
        tag   (-> (spec/sample ::core-spec/tag) (assoc :commits shas))]
    (is (= (count pulls) (count (:pulls (sut/assoc-pulls pulls tag)))))))

(defn- sample-tag []
  (-> (spec/sample ::git/tag)
      (assoc :name (spec/sample ::semver/version-string))))

(deftest changelog
  (let [config (spec/sample ::config/config-map)
        repo   (spec/sample ::git/repo)
        pulls  (gen/sample (s/gen ::github/pull))
        tags   (take 10 (repeatedly sample-tag))]
    (with-redefs [git/init           (constantly repo)
                  git/tags           (constantly tags)
                  git/commits        (constantly [])
                  github/fetch-pulls (constantly pulls)]
      (is (= (count tags) (count (sut/changelog config)))))))

(comment
  (gen/sample (s/gen ::git/tag {::git/name #(s/gen ::semver/version-string)})))
