(ns github-changelog.markdown
  (:require [clojure.string :as str]))

(defn- block-item [body]
  (str \newline body \newline \newline))

(defn- header [n body]
  (block-item (str \newline (str/join (repeat n "#")) " " body)))

(def h1 #(header 1 %))
(def h2 #(header 2 %))
(def h3 #(header 3 %))
(def h4 #(header 4 %))
(def h5 #(header 5 %))
(def h6 #(header 6 %))

(defn link
  ([url] (link url url))
  ([text url] (format "[%s](%s)" text url)))

(defn emphasis [text] (format "**%s**" text))

(defn li [body]
  (let [lines      (str/split-lines body)
        first-line (str "* " (first lines))
        rest-lines (mapv #(str "  " %) (rest lines))]
    (str \newline (str/join \newline (into [first-line] rest-lines)))))
