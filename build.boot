(def project 'github-changelog)

(set-env!
 :source-paths #{"src/"}
 :dependencies
 '[[org.clojure/clojure "1.8.0"]
   [clj-jgit "0.8.10"]
   [clj-http "3.8.0"]
   [cheshire "5.8.0"]
   [throttler "1.0.0"]
   [org.slf4j/slf4j-nop "1.7.25"]
   [grimradical/clj-semver "0.3.0" :exclusions [org.clojure/clojure]]
   [org.clojure/tools.cli "0.3.5"]
   ; testing
   [degree9/boot-semver "1.4.3" :scope "test"]
   [org.clojure/test.check "0.9.0" :scope "test"]
   [clj-http-fake "1.0.3" :scope "test"]
   [adzerk/boot-test "1.2.0" :scope "test"]
   [tolitius/boot-check "0.1.9" :scope "test"]])

(require
 '[adzerk.boot-test :as boot-test]
 '[tolitius.boot-check :as check]
 '[degree9.boot-semver :refer :all])

(task-options!
 pom {:project project
      :version (get-version)
      :description "GitHub changelog"
      :url "https://github.com/whitepages/github-changelog"
      :license {"MIT" "http://choosealicense.com/licenses/mit/"}}
 jar {:file (format "%s-%s.jar" project (get-version))
      :main 'github-changelog.cli}
 aot {:namespace #{'github-changelog.cli}})

(deftask testing-helper
  "Sets up the environment for testing"
  []
  (merge-env! :source-paths #{"test"})
  identity)

(deftask check-sources
  "Checks source code for possible improvements/simplifications"
  []
  (comp
   (testing-helper)
   (check/with-bikeshed)
   (check/with-eastwood)
   (check/with-yagni :options {:entry-points ["github-changelog.cli/-main"]})
   (check/with-kibit)))

(deftask dev []
  "Sets up a development environment"
  (comp (testing-helper)
        (repl)))

(ns-unmap 'boot.user 'test)

(deftask test
  "Runs the test suite"
  []
  (comp
   (testing-helper)
   (boot-test/test)))

(deftask auto-test
  "Tests everything whenever source changes"
  []
  (comp
   (testing-helper)
   (watch)
   (boot-test/test)))

(deftask uberjar
  []
  (comp
   (version :include true)
   (uber)
   (aot)
   (pom)
   (jar)
   (sift)
   (target)))
