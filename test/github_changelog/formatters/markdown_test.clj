(ns github-changelog.formatters.markdown-test
  (:require [clojure
             [string :refer [join]]
             [test :refer :all]]
            [github-changelog
             [markdown :as markdown]
             [schema-generators :as sgen :refer [complete-pull complete-tag]]
             [version-examples :refer :all]]
            [github-changelog.formatters.markdown :as f-markdown]))

(deftest highlight-fn
  (are [function version] (= function (f-markdown/highlight-fn version))
    markdown/h1 v-major
    markdown/h2 v-minor
    markdown/h3 v-patch
    markdown/h4 v-pre-release
    markdown/h5 v-build))

(deftest format-tag
  (are [content tag] (= content (f-markdown/format-tag tag))
    (markdown/h1 "v1.0.0") (complete-tag {:name "v1.0.0" :version v-major})
    (markdown/h2 "v1.1.0") (complete-tag {:name "v1.1.0" :version v-minor})))

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

(def expected-scopeless-changes (str (markdown/h6 "Features")
                                  expected-changes))

(def expected-scope (markdown/emphasis "scope:"))
(def expected-scoped-changes (str (markdown/h6 "Features")
                                  (markdown/li (join [expected-scope expected-changes]))))

(deftest format-changes
  (testing "scopeless changes"
    (is (= expected-scopeless-changes (f-markdown/format-changes ["feat" [(change "") (change "")]]))))
  (testing "scoped changes"
    (is (= expected-scoped-changes (f-markdown/format-changes ["feat" [(change) (change)]])))))
