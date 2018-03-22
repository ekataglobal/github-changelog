(ns github-changelog.formatters.markdown-test
  (:require [clojure
             [string :as str]
             [test :refer :all]]
            [github-changelog
             [markdown :as markdown]
             [schema-generators :as sgen]
             [version-examples :as v]]
            [github-changelog.formatters.markdown :as sut]))

(deftest format-tag
  (are [content tag] (= content (sut/format-tag tag))
    (markdown/h2 (markdown/emphasis "Major")) (sgen/complete-tag {:name "Major" :version v/major})
    (markdown/h2 "Minor") (sgen/complete-tag {:name "Minor" :version v/minor})
    (markdown/h3 (markdown/emphasis "Patch")) (sgen/complete-tag {:name "Patch" :version v/patch})
    (markdown/h3 "Pre-release") (sgen/complete-tag {:name "Pre-release" :version v/pre-release})
    (markdown/h4 (markdown/emphasis "Build")) (sgen/complete-tag {:name "Build" :version v/build})))

(def pull (sgen/complete-pull {:number 1 :html_url "http://example.com/"}))

(defn- change
  ([] (change "scope"))
  ([scope]
   (sgen/complete-change {:type         "feat"
                          :scope        scope
                          :subject      "new something"
                          :pull-request pull
                          :issues       []})))

(def expected-change (str "new something " (markdown/link "#1" "http://example.com/")))

(def expected-changes (str/join (map markdown/li [expected-change expected-change])))

(def expected-scopeless-changes (str (markdown/h4 "Features")
                                  expected-changes))

(def expected-scope (markdown/emphasis "scope:"))
(def expected-scoped-changes (str (markdown/h4 "Features")
                                  (markdown/li (str/join [expected-scope expected-changes]))))

(deftest format-changes
  (testing "scopeless changes"
    (is (= expected-scopeless-changes (sut/format-changes ["feat" [(change "") (change "")]]))))
  (testing "scoped changes"
    (is (= expected-scoped-changes (sut/format-changes ["feat" [(change) (change)]])))))
