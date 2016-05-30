(ns github-changelog.schema-generators
  (:require [clojure.string :refer [join]]
            [clojure.test.check.generators :as gen]))

(def string-std (gen/not-empty gen/string-alphanumeric))

(def hexadecimal (gen/elements "0123456789ABCDEF"))

(def sha (gen/fmap join (gen/vector hexadecimal 40)))

(def issue (gen/tuple string-std string-std))

(def revert-title (gen/fmap (partial format "Revert %s") string-std))

(def revert-body (gen/fmap (partial apply format "Reverts %s/%s#%d")
                           (gen/tuple string-std string-std gen/nat)))

(def string-title (gen/fmap (partial apply format "%s(%s): %s")
                            (gen/tuple string-std string-std string-std)))

(defn complete [gen partial-datum]
  (merge (gen/generate gen) partial-datum))

(defmacro defgen [fn-name kv]
  `(do
     (def ~fn-name (apply gen/hash-map (flatten (seq ~kv))))
     (def ~(symbol (str "complete-" fn-name)) (partial complete ~fn-name))))

(def pull-data
  {:title    string-std
   :number   gen/nat
   :sha      sha
   :body     string-std
   :html_url string-std})

(defgen config
  {:user string-std
   :repo string-std})

(defgen semver
  {:major gen/nat
   :minor gen/nat
   :patch gen/nat})

(defgen pull
  pull-data)

(defgen valid-pull
  (merge pull-data {:title string-title}))

(defgen revert-pull
  (merge pull-data  {:title revert-title
                     :body  revert-body}))

(defgen change
  {:type         gen/string-ascii
   :scope        gen/string-ascii
   :subject      gen/string-ascii
   :pull-request pull
   :issues       (gen/vector issue)})

(defgen tag
  {:name string-std
   :sha  sha})
