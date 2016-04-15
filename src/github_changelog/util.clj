(ns github-changelog.util
  (:require [clojure.string :refer [ends-with?]]))

(defn str-map [f & sqs] (apply str (apply map f sqs)))

(defn extract-params [query-string]
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" query-string)]
             [(keyword k) v])))

(defn strip-trailing
  ([s] (strip-trailing s "/"))
  ([s end]
   (if (ends-with? s end)
     (recur (apply str (drop-last s)) end)
     s)))
