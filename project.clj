(defproject hu.ssh/github-changelog "0.1.0-SNAPSHOT"
  :description "GitHub changelog"
  :url "https://github.com/raszi/github-changelog"
  :main github-changelog.cli
  :repl-options {:init-ns user}
  :license {:name "MIT"
            :url  "http://choosealicense.com/licenses/mit/"}
  :plugins [[lein-cloverage "1.0.6"]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [prismatic/schema "1.0.5"]
                 [environ "1.0.2"]
                 [org.clojure/tools.cli "0.3.3"]
                 [clj-jgit "0.8.8"]
                 [clj-http "2.1.0"]
                 [cheshire "5.5.0"]
                 [throttler "1.0.0"]
                 [org.slf4j/slf4j-nop "1.7.18"]
                 [grimradical/clj-semver "0.3.0-20130920.191002-3" :exclusions [org.clojure/clojure]]]
  :profiles {:dev     {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                      [org.clojure/test.check "0.9.0"]
                                      [clj-http-fake "1.0.2"]]
                       :source-paths ["dev"]}
             :uberjar {:aot :all}})
