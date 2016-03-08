(ns github-changelog.semver
  (:require [clj-semver.core :as semver]))

(def newer? semver/newer?)

(defn parse [version]
  (try (semver/parse version)
       (catch java.lang.AssertionError _e nil)))

(defn extract [tag-name]
  (parse
   (if (= \v (first tag-name))
     (subs tag-name 1)
     tag-name)))
