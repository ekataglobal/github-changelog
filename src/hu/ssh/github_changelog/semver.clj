(ns hu.ssh.github-changelog.semver
  (:require [clj-semver.core :as semver]))

(defn semver? [x] (or (nil? x) (every? x [:major :minor :patch])))

(def newer? semver/newer?)

(defn- parse [version]
  {:pre  [(string? version)]
   :post [(semver? %)]}
  (try (semver/parse version)
       (catch java.lang.AssertionError _e nil)))

(defn extract [tag-name]
  {:pre  [(string? tag-name)]
   :post [(semver? %)]}
  (parse (if (= \v (first tag-name)) (subs tag-name 1) tag-name)))
