(ns hu.ssh.github-changelog.dependencies.bundler
  (:require
    [clojure.string :refer [split-lines]]
    [clojure.java.io :refer [reader]]))

(defn- get-specs [reader]
  (->> (doall (line-seq reader))
       (drop-while #(not= % "  specs:"))
       (drop 1)
       (take-while seq)))

(defn- parse-spec [spec]
  {:name (second spec) :version (nth spec 2)})

(defn- parse-specs [specs]
  (->> (map #(re-matches #"^\s{4}(\S+) \((.*)\)$" %) specs)
       (remove empty?)
       (map parse-spec)))

(defn parse [file]
  (with-open [rdr (reader file)]
    (parse-specs (get-specs rdr))))
