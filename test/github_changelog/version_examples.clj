(ns github-changelog.version-examples
  (:require
   [github-changelog.schema-generators :refer [complete-semver]]))

(def v-major (complete-semver {:major 1 :minor 0, :patch 0, :pre-release nil :build nil}))
(def v-minor (complete-semver {:minor 1 :patch 0, :pre-release nil :build nil}))
(def v-patch (complete-semver {:patch 1 :pre-release nil :build nil}))
(def v-pre-release (complete-semver {:pre-release "pre" :build nil}))
(def v-build (complete-semver {:pre-release "pre" :build "42"}))
