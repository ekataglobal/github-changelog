(ns github-changelog.semver
  (:require
    [github-changelog.schema :refer [Semver]]
    [clj-semver.core :as semver]
    [schema.core :as s]))

(def newer? semver/newer?)

(s/defn parse :- (s/maybe Semver)
  [version :- s/Str]
  (try (semver/parse version)
       (catch java.lang.AssertionError _e nil)))

(s/defn extract :- (s/maybe Semver)
  [tag-name :- s/Str]
  (parse (if (= \v (first tag-name)) (subs tag-name 1) tag-name)))

(defn get-type [{:keys [minor patch pre-release build]}]
  (cond
    (= [0 0 "" ""] [minor patch pre-release build]) :major
    (= [0 "" ""] [patch pre-release build]) :minor
    (= ["" ""] [pre-release build]) :patch
    (= "" build) :pre-release
    :else :build))
