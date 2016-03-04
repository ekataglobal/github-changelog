(ns github-changelog.schema-generators
  (:require [github-changelog.schema :as schema]
            [clojure.string :refer [join]]
            [schema.experimental.generators :as gen]
            [clojure.test.check.generators :as check-generators]))

(def hexadecimal
  (check-generators/elements "0123456789ABCDEF"))

(def sha
  ((gen/fmap join) (check-generators/vector hexadecimal 40)))

(def generators {schema/Sha sha})
