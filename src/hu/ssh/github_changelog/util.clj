(ns hu.ssh.github-changelog.util
  (:require [clj-semver.core :refer [parse]]))

(def git-url (partial format "%s/%s/%s.git"))

(defn value-at
  [ks m]
  {:pre  [(map? m) (vector? ks)]
   :post [(string? %)]}
  (get-in m ks))

(defn extract-semver
  "Extracts semantic versions with or without 'v' predicate from the tags"
  [tag]
  {:pre  [(:name tag)]
   :post [(or (nil? %) (:major %))]}
  (let [version (:name tag)
        parse #(try (parse %)
                    (catch java.lang.AssertionError _e nil))]
    (parse
      (if (= \v (first version))
        (subs version 1)
        version))))
