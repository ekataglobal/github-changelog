(defproject hu.ssh/github-changelog "0.1.0-SNAPSHOT"
  :description "GitHub changelog"
  :url "https://github.com/raszi/github-changelog"
  :main hu.ssh.github-changelog.cli
  :repl-options {:init-ns user}
  :license {:name "MIT"
            :url "http://choosealicense.com/licenses/mit/"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [prismatic/schema "1.0.3"]
                 [environ "1.0.1"]
                 [org.clojure/tools.cli "0.3.3"]
                 [clj-jgit "0.8.8"]
                 [clj-http "2.0.0"]
                 [cheshire "5.5.0"]
                 [throttler "1.0.0"]
                 [org.slf4j/slf4j-nop "1.7.13"]
                 [grimradical/clj-semver "0.3.0-20130920.191002-3" :exclusions [org.clojure/clojure]]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/test.check "0.9.0"]]
                   :source-paths ["dev"]}
             :uberjar {:aot :all}})
