(ns github-changelog.version-examples
  (:require
   [github-changelog.schema-generators :refer [complete-semver]]))

(def v-major (complete-semver {:major 1 :minor 0, :patch 0, :pre-release "" :build ""}))
(def v-minor (complete-semver {:minor 1 :patch 0, :pre-release "" :build ""}))
(def v-patch (complete-semver {:patch 1 :pre-release "" :build ""}))
(def v-pre-release (complete-semver {:pre-release "pre" :build ""}))
(def v-build (complete-semver {:pre-release "pre" :build "42"}))
