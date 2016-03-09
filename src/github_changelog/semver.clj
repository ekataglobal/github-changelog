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

(defn get-type [{:keys [minor patch pre-release build]}]
  (cond
    (= [0 0 "" ""] [minor patch pre-release build]) :major
    (= [0 "" ""] [patch pre-release build]) :minor
    (= ["" ""] [pre-release build]) :patch
    (= "" build) :pre-release
    :else :build))
