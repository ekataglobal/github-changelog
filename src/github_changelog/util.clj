(ns github-changelog.util
  (:require [clojure.string :refer [join]]))

(def git-url (partial format "%s/%s/%s.git"))

(defn str-map [f & sqs] (join (apply map f sqs)))

(defn extract-params [query-string]
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" query-string)]
             [(keyword k) v])))

(defn strip-trailing [str end]
  (if (.endsWith str end)
    (recur (.substring str 0 (- (count str) (count end))) end)
    str))
