(ns hu.ssh.github-changelog.formatters.markdown-test
  (:require
    [hu.ssh.github-changelog.formatters.markdown :as f-markdown]
    [hu.ssh.github-changelog.markdown :as markdown]
    [hu.ssh.github-changelog.schema :refer [Semver Tag Change Pull Issue]]
    [hu.ssh.github-changelog.util :refer [gen-sha]]
    [clojure.test :refer :all]
    [schema.experimental.complete :as c]))

(def v-major (c/complete {:major 1 :minor 0, :patch 0, :pre-release nil :build nil} Semver))
(def v-minor (c/complete {:minor 1 :patch 0, :pre-release nil :build nil} Semver))
(def v-patch (c/complete {:patch 1 :pre-release nil :build nil} Semver))
(def v-pre-release (c/complete {:pre-release "pre" :build nil} Semver))
(def v-build (c/complete {:build "42"} Semver))

(deftest highlight-fn
         (are [function version] (= (f-markdown/highlight-fn version) function)
              markdown/h1 v-major
              markdown/h2 v-minor
              markdown/h3 v-patch
              markdown/h4 v-pre-release
              markdown/h5 v-build))

(deftest format-tag
  (are [content tag] (= content (f-markdown/format-tag tag))
                     (markdown/h1 "v1.0.0") (c/complete {:name "v1.0.0" :sha (gen-sha) :version v-major} Tag)
                     (markdown/h2 "v1.1.0") (c/complete {:name "v1.1.0" :sha (gen-sha) :version v-minor} Tag)))

(def pull (c/complete {:number 1 :html_url "http://example.com/" :sha (gen-sha)} Pull))
(def change (c/complete {:type "feat" :scope "scope" :subject "new something" :pull-request pull :issues []} Change))

(def expected-change "new something [#1](http://example.com/)")

(deftest format-change
  (is (= expected-change (f-markdown/format-change change))))

(def grouped (group-by :scope [change change]))
(def expected-scope (markdown/emphasis "scope:"))

(deftest format-grouped-changes
  (is (= (str expected-scope " " expected-change) (f-markdown/format-grouped-changes ["scope" [change]])))
  (is (= (str expected-scope (markdown/ul [expected-change expected-change])) (f-markdown/format-grouped-changes (first grouped)))))
