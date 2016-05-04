(ns github-changelog.util
  (:require [clojure.string :refer [join ends-with?]]))

(defn str-map [f & sqs] (join (apply map f sqs)))

(defn extract-params [query-string]
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" query-string)]
             [(keyword k) v])))

(defn strip-trailing
  ([s] (strip-trailing s "/"))
  ([s end]
   (if (ends-with? s end)
     (recur (join (drop-last s)) end)
     s)))
