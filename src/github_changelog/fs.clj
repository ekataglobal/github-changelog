(ns github-changelog.fs
  (:require [clojure.java.io :as io]
            [babashka.fs :as fs])
  (:import java.io.File
           [java.nio.file Files Path]
           java.nio.file.attribute.FileAttribute))

(def empty-file-attrs (into-array FileAttribute []))

(defn tmp []
  (System/getProperty "java.io.tmpdir"))

(defn- as-path ^Path [^String str]
  (.toPath (File. str)))

(defn as-file
  (^File [f] (io/as-file f))
  (^File [^String parent ^String child] (File. parent child)))

(defn basename
  "Returns the filename or the directory portion of the pathname"
  ^String [^String f]
  (.toString (.getFileName (as-path f))))

(defn tmp-file
  "Creates a temporary file"
  ([] (tmp-file nil))
  ([dir] (tmp-file dir nil))
  ([dir prefix] (tmp-file dir prefix nil))
  ([dir ^String prefix ^String suffix]
   (.toString (Files/createTempFile (as-path (or dir (tmp))) prefix suffix empty-file-attrs))))

(defn tmp-dir
  "Creates a temporary directory"
  ([] (tmp-dir nil))
  ([dir] (tmp-dir dir nil))
  ([dir prefix]
   (.toString (Files/createTempDirectory (as-path (or dir (tmp))) prefix empty-file-attrs))))

(defn exists? [file]
  (.exists (as-file file)))

(defn file? [file]
  (.isFile (as-file file)))

(defn dir? [file]
  (.isDirectory (as-file file)))

(defn rm
  "Deletes file"
  [file]
  (.delete (as-file file)))

(defn rm-dir
  "Deletes directory recursively"
  [dir]
  (fs/delete-tree dir))
