(ns github-changelog.formatters.markdown-test
  (:require [clojure
             [string :refer [join]]
             [test :refer :all]]
            [github-changelog
             [markdown :as markdown]
             [schema-generators :as sgen :refer [complete-pull complete-tag]]
             [version-examples :refer :all]]
            [github-changelog.formatters.markdown :as f-markdown]))

(deftest format-tag
  (are [content tag] (= content (f-markdown/format-tag tag))
    (markdown/h2 (markdown/emphasis "Major")) (complete-tag {:name "Major" :version v-major})
    (markdown/h2 "Minor") (complete-tag {:name "Minor" :version v-minor})
    (markdown/h3 (markdown/emphasis "Patch")) (complete-tag {:name "Patch" :version v-patch})
    (markdown/h3 "Pre-release") (complete-tag {:name "Pre-release" :version v-pre-release})
    (markdown/h4 (markdown/emphasis "Build")) (complete-tag {:name "Build" :version v-build})))

(def pull (complete-pull {:number 1 :html_url "http://example.com/"}))

(defn- change
  ([] (change "scope"))
  ([scope]
   (sgen/complete-change {:type         "feat"
                          :scope        scope
                          :subject      "new something"
                          :pull-request pull
                          :issues       []})))

(def expected-change (str "new something " (markdown/link "#1" "http://example.com/")))

(def expected-changes (join (map markdown/li [expected-change expected-change])))

(def expected-scopeless-changes (str (markdown/h4 "Features")
                                  expected-changes))

(def expected-scope (markdown/emphasis "scope:"))
(def expected-scoped-changes (str (markdown/h4 "Features")
                                  (markdown/li (join [expected-scope expected-changes]))))

(deftest format-changes
  (testing "scopeless changes"
    (is (= expected-scopeless-changes (f-markdown/format-changes ["feat" [(change "") (change "")]]))))
  (testing "scoped changes"
    (is (= expected-scoped-changes (f-markdown/format-changes ["feat" [(change) (change)]])))))
