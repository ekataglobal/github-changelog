(ns github-changelog.schema-complete
  (:require
    [github-changelog.schema-generators :refer [generators]]
    [schema.experimental.complete :as c]))

(defn complete [partial-datum schema]
  (c/complete partial-datum schema {} generators))
