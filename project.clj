(defproject hu.ssh/github-changelog "0.1.0-SNAPSHOT"
  :description "GitHub changelog"
  :url "https://github.com/raszi/github-changelog"
  :main hu.ssh.github-changelog.cli
  :license {:name "MIT"
            :url "http://choosealicense.com/licenses/mit/"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [prismatic/schema "1.0.2"]
                 [environ "1.0.1"]
                 [org.clojure/tools.cli "0.3.3"]
                 [clj-jgit "0.8.8"]
                 [tentacles "0.3.0"]
                 [grimradical/clj-semver "0.3.0-20130920.191002-3" :exclusions [org.clojure/clojure]]
                 [org.clojure/test.check "0.8.2"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
                   :source-paths ["dev"]}
             :uberjar {:aot :all}})
