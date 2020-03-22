(ns github-changelog.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str])
  (:import java.net.URL))

(s/def ::non-blank-string (s/and string? (complement str/blank?)))

(defn url? [x]
  (try
    (doto (URL. x)
      (.toURI))
    true
    (catch Exception _e
      false)))

(defn url-gen []
  (gen/fmap str (s/gen uri?)))

(s/def ::url
  (s/with-gen
    url?
    url-gen))

(def hex-char? #{\0 \1 \2 \3 \4 \5 \7 \8 \9 \a \b \c \d \e \f})

(defn sha-gen []
  (->> (s/coll-of hex-char? :count 40)
       (s/gen)
       (gen/fmap str/join)))

(s/def ::sha
  (s/with-gen
    (s/and string? #(= 40 (count %)) #(every? hex-char? %))
    sha-gen))

(defn sample
  ([spec] (sample spec nil))
  ([spec overrides]
   (-> (s/exercise spec 10 overrides) rand-nth first)))

(comment
  (s/exercise ::non-blank-string)
  (s/exercise ::url)
  (s/exercise ::sha))
