(ns github-changelog.util
  (:require [clojure.string :refer [join ends-with?]]))

(defn str-map [f & sqs] (join (apply map f sqs)))

(defn extract-params [query-string]
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" query-string)]
             [(keyword k) v])))

(defn strip-trailing
  ([str] (strip-trailing str "/"))
  ([str end]
   (if (ends-with? str end)
     (recur (.substring str 0 (- (count str) (count end))) end)
     str)))
