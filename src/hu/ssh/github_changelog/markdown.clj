(ns hu.ssh.github-changelog.markdown
  (:require [clojure.string :refer [join]]))

(defn header [n body] (str (apply str (repeat n "#")) " " body \newline \newline))

(defn link
  ([url] (link url url))
  ([text url] (format "[%s](%s)" text url)))

(defn emphasis [text] (format "**%s**" text))

(defn ul [items]
  (as-> items it
        (map (partial format "* %s") it)
        (join \newline it)
        (str it \newline \newline)))
