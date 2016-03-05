(ns github-changelog.schema-generators
  (:require [github-changelog.schema :as schema]
            [clojure.string :refer [join]]
            [schema.experimental.generators :as gen]
            [clojure.test.check.generators :as check-generators]))

(def hexadecimal
  (check-generators/elements "0123456789ABCDEF"))

(def sha
  ((gen/fmap join) (check-generators/vector hexadecimal 40)))

(def natural
  (check-generators/choose 0 Long/MAX_VALUE))

(def generators {schema/Sha sha
                 schema/Natural natural})

(defn generate [schema]
  (gen/generate schema generators))

(defn sample [samples schema]
  (gen/sample samples schema generators))
