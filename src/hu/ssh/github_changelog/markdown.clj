(ns hu.ssh.github-changelog.markdown)

(defn header [n body] (str (apply str (repeat n "#")) " " body))
