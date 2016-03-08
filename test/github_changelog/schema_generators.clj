(ns github-changelog.schema-generators
  (:require [github-changelog.schema :as schema]
            [clojure.string :refer [join]]
            [clojure.test.check.generators :as gen]))

(def hexadecimal (gen/elements "0123456789ABCDEF"))

(def sha (gen/fmap join (gen/vector hexadecimal 40)))

(def issue (gen/tuple gen/string gen/string))

(defmacro defrecord-gen [name record-fn & body]
  `(def ~name (gen/fmap ~record-fn (gen/hash-map ~@body))))

(defrecord-gen config schema/map->Config
  :git gen/string
  :github-api gen/string
  :user gen/string
  :repo gen/string)

(defrecord-gen semver schema/map->Semver
  :major gen/nat
  :minor gen/nat
  :patch gen/nat)

(defrecord-gen pull schema/map->Pull
  :title (gen/not-empty gen/string-alphanumeric)
  :number gen/nat
  :sha sha
  :body gen/string
  :html_url gen/string)

(defrecord-gen change schema/map->Change
  :type gen/string
  :scope gen/string
  :subject gen/string
  :pull-request pull
  :issues (gen/vector issue))

(defrecord-gen tag schema/map->Tag
  :name (gen/not-empty gen/string-alphanumeric)
  :sha sha)
