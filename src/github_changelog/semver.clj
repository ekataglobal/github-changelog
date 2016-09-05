(ns github-changelog.semver
  (:require [clj-semver.core :as semver]
            [clojure.string :refer [replace-first]]))

(def newer? semver/newer?)

(defn parse [version]
  (try (semver/parse version)
       (catch java.lang.AssertionError _e nil)))

(defn extract [tag-name prefix]
  (let [pattern (re-pattern (str "^" prefix))]
    (parse (replace-first tag-name pattern ""))))

(defn get-type [{:keys [minor patch pre-release build]}]
  (cond
    (= [0 0 nil nil] [minor patch pre-release build]) :major
    (= [0 nil nil] [patch pre-release build]) :minor
    (= [nil nil] [pre-release build]) :patch
    (= nil build) :pre-release
    :else :build))
