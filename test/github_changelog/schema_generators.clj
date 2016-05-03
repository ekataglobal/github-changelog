(ns github-changelog.schema-generators
  (:require [clojure.string :refer [join]]
            [clojure.test.check.generators :as gen]))

(def hexadecimal (gen/elements "0123456789ABCDEF"))

(def sha (gen/fmap join (gen/vector hexadecimal 40)))

(def issue (gen/tuple gen/string gen/string))

(defn complete
  ([gen] #(complete % gen))
  ([partial-datum gen]
   (merge (gen/generate gen) partial-datum)))

(defmacro defgen [fn-name & body]
  `(do
     (def ~fn-name (gen/hash-map ~@body))
     (def ~(symbol (str "complete-" fn-name)) (complete ~fn-name))))

(defgen config
  :user gen/string
  :repo gen/string)

(defgen semver
  :major gen/nat
  :minor gen/nat
  :patch gen/nat)

(defgen pull
  :title (gen/not-empty gen/string-alphanumeric)
  :number gen/nat
  :sha sha
  :body gen/string
  :html_url gen/string)

(defgen change
  :type gen/string
  :scope gen/string
  :subject gen/string
  :pull-request pull
  :issues (gen/vector issue))

(defgen tag
  :name (gen/not-empty gen/string-alphanumeric)
  :sha sha)
