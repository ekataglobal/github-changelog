(ns github-changelog.validators
  (:import (java.net URL)))

(defn min-length [min]
  [#(>= (count %) min) (format "Should be at least %s character(s)" min)])

(defn- valid-url? [url]
  (try (some? (URL. url)) (catch Exception _ false)) )

(defn url []
  [#(valid-url? %) "Should be a valid URL"])
