(ns hu.ssh.github-changelog.markdown)

(defn header [n body] (str (apply str (repeat n "#")) " " body))

(defn link
  ([url] (link url url))
  ([text url] (format "[%s](%s)" text url)))
