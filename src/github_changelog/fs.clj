(ns github-changelog.fs
  (:require [clojure.java.io :as io])
  (:import java.io.File))

(defn as-file
  (^File [f] (io/as-file f))
  (^File [^String parent ^String child] (File. parent child)))

(defn dir? [file]
  (.isDirectory (as-file file)))
