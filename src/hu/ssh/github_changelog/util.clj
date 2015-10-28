(ns hu.ssh.github-changelog.util
  (:require [clojure.string :refer [join]]))

(def git-url (partial format "%s/%s/%s.git"))

(defn gen-sha [] (join (repeatedly 40 #(rand-nth "0123456789ABCDEF"))))

(defn str-map [f & sqs] (join (apply map f sqs)))
