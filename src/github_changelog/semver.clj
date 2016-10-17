(ns github-changelog.semver
  (:require [clj-semver.core :as semver]
            [clojure.string :as str]))

(def newer? semver/newer?)

(defn parse [version]
  (try (semver/parse version)
       (catch java.lang.AssertionError _e nil)))

(defn extract [tag-name prefix]
  (parse
   (if (str/starts-with? tag-name prefix)
     (subs tag-name (count prefix))
     tag-name)))

(defn get-type [{:keys [minor patch pre-release build]}]
  (cond
    (= [0 0 nil nil] [minor patch pre-release build]) :major
    (= [0 nil nil] [patch pre-release build]) :minor
    (= [nil nil] [pre-release build]) :patch
    (= nil build) :pre-release
    :else :build))
