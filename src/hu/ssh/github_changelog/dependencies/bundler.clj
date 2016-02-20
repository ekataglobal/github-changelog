(ns hu.ssh.github-changelog.dependencies.bundler
  (:require
    [clojure.string :refer [split-lines]]))

(defn- get-specs [file]
  (->> (split-lines file)
       (drop-while #(not= % "  specs:"))
       (drop 1)
       (take-while seq)))

(defn- parse-spec [spec]
  {:gem (second spec) :version (nth spec 2)})

(defn- parse-specs [specs]
  (->> (map #(re-matches #"^\s{4}(\S+) \((.*)\)$" %) specs)
       (remove empty?)
       (map parse-spec)))

(defn parse [file]
  (->> (slurp file)
       get-specs
       parse-specs))
