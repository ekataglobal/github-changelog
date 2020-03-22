(ns github-changelog.formatters.markdown-test
  (:require [clojure.string :as str]
            [clojure.test :refer [are deftest is testing]]
            [github-changelog.conventional :as conventional]
            [github-changelog.core-spec :as core-spec]
            [github-changelog.formatters.markdown :as sut]
            [github-changelog.github :as github]
            [github-changelog.markdown :as md]
            [github-changelog.semver :as semver]
            [github-changelog.spec :as spec]))

(defn sample-tag [overrides]
  (-> (spec/sample ::core-spec/tag)
      (merge overrides)))

(deftest format-tag
  (are [content tag] (= content (sut/format-tag tag))
    (md/h2 (md/emphasis "Major")) (sample-tag {:name "Major" :version (spec/sample ::semver/major-version)})
    (md/h2 "Minor")               (sample-tag {:name "Minor" :version (spec/sample ::semver/minor-version)})
    (md/h3 (md/emphasis "Patch")) (sample-tag {:name "Patch" :version (spec/sample ::semver/patch-version)})
    (md/h3 "Pre-release")         (sample-tag {:name "Pre-release" :version (spec/sample ::semver/pre-release-version)})
    (md/h4 (md/emphasis "Build")) (sample-tag {:name "Build" :version (spec/sample ::semver/build-version)})))

(def pull (-> (spec/sample ::github/pull) (merge {:number 1 :html_url "http://example.com/"})))

(defn- change
  ([] (change "scope"))
  ([scope]
   (-> (spec/sample ::conventional/change)
       (merge  {:type         "feat"
                :scope        scope
                :subject      "new something"
                :pull-request pull
                :issues       [["1" "http://example.com/issue/1"]]}))))

(def expected-change (str "new something " (md/link "#1" "http://example.com/")
                          ", closes " (md/link "1" "http://example.com/issue/1")))

(def expected-changes (str/join (map md/li [expected-change expected-change])))

(def expected-scopeless-changes (str (md/h4 "Features")
                                     expected-changes))

(def expected-scope (md/emphasis "scope:"))
(def expected-scoped-changes (str (md/h4 "Features")
                                  (md/li (str/join [expected-scope expected-changes]))))

(deftest format-change-group
  (testing "scopeless changes"
    (is (= expected-scopeless-changes (sut/format-change-group ["feat" [(change "") (change "")]]))))
  (testing "scoped changes"
    (is (= expected-scoped-changes (sut/format-change-group ["feat" [(change) (change)]])))))
