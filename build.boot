(set-env! :dependencies
          '[[org.clojure/clojure "1.7.0"]
            [org.clojure/core.match "0.3.0-alpha4"]
            [prismatic/schema "1.0.5"]
            [environ "1.0.2"]
            [org.clojure/tools.cli "0.3.3"]
            [clj-jgit "0.8.8"]
            [clj-http "2.1.0"]
            [cheshire "5.5.0"]
            [throttler "1.0.0"]
            [org.slf4j/slf4j-nop "1.7.18"]
            [grimradical/clj-semver "0.3.0-20130920.191002-3" :exclusions [org.clojure/clojure]]
            ; testing
            [org.clojure/tools.namespace "0.2.11" :scope "test"]
            [org.clojure/test.check "0.9.0" :scope "test"]
            [clj-http-fake "1.0.2" :scope "test"]
            [adzerk/boot-test "1.1.1" :scope "test"]]
          :source-paths #{"src/"})

(ns-unmap 'boot.user 'test)

(require
 '[github-changelog.core :refer [changelog]]
 '[adzerk.boot-test :refer :all])

(task-options!
 pom {:project 'hu.ssh/github-changelog
      :version "0.1.0-SNAPSHOT"
      :description "GitHub changelog"
      :url "https://github.com/raszi/github-changelog"
      :license {:name "MIT"
                :url  "http://choosealicense.com/licenses/mit/"}}
 jar {:main 'github-changelog.cli}
 aot {:all true})

(deftask testing-helper []
  "Sets up the environment for testing"
  (merge-env! :source-paths #{"test"})
  identity)

(deftask auto-test []
  "Test everything whenever source changes"
  (comp
   (testing-helper)
   (watch)
   (test)))
