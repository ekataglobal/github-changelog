(ns github-changelog.config
  (:require [clojure.spec.alpha :as s]
            [github-changelog.spec :as spec]))

(s/def ::git-url ::spec/url)

(s/def ::github ::spec/url)

(s/def ::github-api ::spec/url)

(s/def ::jira ::spec/url)

(s/def ::user ::spec/non-blank-string)

(s/def ::repo ::spec/non-blank-string)

(s/def ::dir ::spec/non-blank-string)

(s/def ::update? boolean?)

(s/def ::tag-prefix string?)

(s/def ::token ::spec/non-blank-string)

(s/def ::config-map
  (s/keys :req-un [::user ::repo]
          :opt-un [::token ::github ::github-api ::jira ::dir ::update? ::git-url]))

(def defaults {:github     "https://github.com/"
               :github-api "https://api.github.com/"
               :update?    true
               :tag-prefix "v"})

(comment
  (s/exercise ::config-map))
