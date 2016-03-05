(ns github-changelog.dependencies.bundler-test
  (:require
    [github-changelog.dependencies.bundler :as bundler]
    [clojure.test :refer :all]
    [clojure.java.io :refer [file]]))

(defn- fixture-file [path]
  (.getCanonicalPath (file "test/fixtures" path)))

(deftest format-tag
  (testing "with an empty file"
    (is (empty? (bundler/parse (fixture-file "Gemfile_empty.lock")))))
  (testing "with empty deps"
    (is (empty? (bundler/parse (fixture-file "Gemfile_no-deps.lock"))))))
