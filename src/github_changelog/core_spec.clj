(ns github-changelog.core-spec
  (:require [clojure.spec.alpha :as s]
            [github-changelog.git :as git]
            [github-changelog.github :as github]
            [github-changelog.semver :as semver]))

(s/def ::commits (s/* ::git/commit))

(s/def ::tag
  (s/merge ::git/tag (s/keys :req-un [::semver/version ::commits])))

(s/def ::pulls (s/* ::github/pull))

(s/def ::tag-with-pulls
  (s/merge ::tag (s/keys :req-un [::pulls])))

(comment
  (s/exercise ::tag)
  (s/exercise ::tag-with-pulls))
