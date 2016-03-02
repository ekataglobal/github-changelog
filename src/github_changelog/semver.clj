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
