(ns github-changelog.version-examples
  (:require
    [github-changelog.schema :refer [Semver]]
    [github-changelog.schema-complete :refer [complete]]))

(def v-major (complete {:major 1 :minor 0, :patch 0, :pre-release "" :build ""} Semver))
(def v-minor (complete {:minor 1 :patch 0, :pre-release "" :build ""} Semver))
(def v-patch (complete {:patch 1 :pre-release "" :build ""} Semver))
(def v-pre-release (complete {:pre-release "pre" :build ""} Semver))
(def v-build (complete {:pre-release "pre" :build "42"} Semver))
