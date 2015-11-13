(ns hu.ssh.github-changelog.markdown
  (:require [clojure.string :refer [join]]))

(defn- block-item [body] (str \newline body \newline))

(defn header [n body] (block-item (str (join (repeat n "#")) " " body)))

(def h1 (partial header 1))
(def h2 (partial header 2))
(def h3 (partial header 3))
(def h4 (partial header 4))
(def h5 (partial header 5))

(defn link
  ([url] (link url url))
  ([text url] (format "[%s](%s)" text url)))

(defn emphasis [text] (format "**%s**" text))

(defn ul [items]
  (as-> items it
        (map (partial format "* %s") it)
        (join \newline it)
        (block-item it)))
