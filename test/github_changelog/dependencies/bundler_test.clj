(ns github-changelog.dependencies.bundler-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [github-changelog.dependencies.bundler :as sut]))

(defn- test-parse [path]
  (->> (io/file "test/fixtures" path)
       .getCanonicalPath
       sut/parse))

(deftest parse
  (testing "with an empty file"
    (is (empty? (test-parse "Gemfile_empty.lock"))))
  (testing "with empty deps"
    (is (empty? (test-parse "Gemfile_no-deps.lock"))))
  (testing "with dependencies"
    (is (some #{{:name "rake" :version "10.5.0"}} (test-parse "Gemfile_rake.lock")))))
