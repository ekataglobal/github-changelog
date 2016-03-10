(ns github-changelog.formatters.markdown-test
  (:require
    [github-changelog.formatters.markdown :as f-markdown]
    [github-changelog.markdown :as markdown]
    [github-changelog.schema-generators :as sgen :refer [complete-semver complete-pull complete-tag]]
    [github-changelog.version-examples :refer :all]
    [clojure.string :refer [join]]
    [clojure.test :refer :all]))

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
(def change (sgen/complete-change {:type "feat" :scope "scope" :subject "new something" :pull-request pull :issues []}))

(def expected-change (str "new something " (markdown/link "#1" "http://example.com/")))

(def grouped (group-by :scope [change change]))
(def expected-scope (markdown/emphasis "scope:"))
(def expected-changes (join (map markdown/li [expected-change expected-change])))

(deftest format-grouped-changes
  (testing "with one change"
    (is (= (str expected-scope " " expected-change) (f-markdown/format-grouped-changes ["scope" [change]]))))
  (testing "with multiple changes"
    (is (= (str expected-scope expected-changes) (f-markdown/format-grouped-changes (first grouped))))))

(def expected-formatted-changes (str (markdown/h5 "Features")
                                     (markdown/li (join [expected-scope expected-changes]))))

(deftest format-changes
  (is (= expected-formatted-changes (f-markdown/format-changes ["feat" [change change]]))))
