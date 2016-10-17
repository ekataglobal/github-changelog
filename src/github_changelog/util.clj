(ns github-changelog.util
  (:require [clojure.string :as str]))

(defn str-map [f & sqs] (str/join (apply map f sqs)))

(defn extract-params [query-string]
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" query-string)]
             [(keyword k) v])))

(defn strip-trailing
  ([s] (strip-trailing s "/"))
  ([s end]
   (if (str/ends-with? s end)
     (recur (str/join (drop-last s)) end)
     s)))
