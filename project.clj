(defproject hu.ssh/github-changelog "0.1.0-SNAPSHOT"
  :description "GitHub changelog"
  :url "https://github.com/raszi/github-changelog"
  :license {:name "MIT"
            :url "http://choosealicense.com/licenses/mit/"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [environ "1.0.1"]
                 [tentacles "0.3.0"]
                 [grimradical/clj-semver "0.3.0-20130920.191002-3" :exclusions [org.clojure/clojure]]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
                   :source-paths ["dev"]}})
