(ns hu.ssh.github-changelog.util
  (:require [clojure.string :refer [join]]))

(def git-url (partial format "%s/%s/%s.git"))

(defn str-map [f & sqs] (join (apply map f sqs)))
