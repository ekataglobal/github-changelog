(ns github-changelog.version-examples
  (:require [github-changelog.schema-generators :as sgen]))

(def major (sgen/complete-semver {:major 1 :minor 0, :patch 0, :pre-release nil :build nil}))
(def minor (sgen/complete-semver {:minor 1 :patch 0, :pre-release nil :build nil}))
(def patch (sgen/complete-semver {:patch 1 :pre-release nil :build nil}))
(def pre-release (sgen/complete-semver {:pre-release "pre" :build nil}))
(def build (sgen/complete-semver {:pre-release "pre" :build "42"}))
