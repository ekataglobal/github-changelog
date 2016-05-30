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

(defn complete
  ([gen] #(complete % gen))
  ([partial-datum gen]
   (merge (gen/generate gen) partial-datum)))

(defmacro defgen [fn-name & body]
  `(do
     (def ~fn-name (gen/hash-map ~@body))
     (def ~(symbol (str "complete-" fn-name)) (complete ~fn-name))))

(defgen config
  :user string-std
  :repo string-std)

(defgen semver
  :major gen/nat
  :minor gen/nat
  :patch gen/nat)

(defgen pull
  :title string-std
  :number gen/nat
  :sha sha
  :body string-std
  :html_url string-std)

(defgen valid-pull
  :title string-title
  :number gen/nat
  :sha sha
  :body string-std
  :html_url string-std)

(defgen revert-pull
  :title revert-title
  :number gen/nat
  :sha sha
  :body revert-body
  :html_url string-std)

(defgen change
  :type gen/string-ascii
  :scope gen/string-ascii
  :subject gen/string-ascii
  :pull-request pull
  :issues (gen/vector issue))

(defgen tag
  :name string-std
  :sha sha)
