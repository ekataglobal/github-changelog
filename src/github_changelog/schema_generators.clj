(ns github-changelog.schema-generators
  (:require [github-changelog.schema :as schema]
            [clojure.string :refer [join upper-case]]
            [schema.experimental.generators :as gen]
            [clojure.test.check.generators :as check-generators]))

(def sha
  ((gen/fmap join) (check-generators/vector (check-generators/elements "0123456789ABCDEF") 40)))

(def generators {schema/Sha sha})
