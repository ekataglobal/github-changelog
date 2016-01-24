(ns hu.ssh.github-changelog.util
  (:require [clojure.string :refer [join]]))

(def git-url (partial format "%s/%s/%s.git"))

(defn gen-sha [] (join (repeatedly 40 #(rand-nth "0123456789ABCDEF"))))

(defn str-map [f & sqs] (join (apply map f sqs)))

(defn extract-params [query-string]
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" query-string)]
             [(keyword k) v])))

(defn strip-trailing [str end]
  (if (.endsWith str end)
    (.substring str 0 (- (count str) (count end)))
    str))
