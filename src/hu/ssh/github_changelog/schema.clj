(ns hu.ssh.github-changelog.schema
  (:require [schema.core :as s]))

(s/defschema Config {s/Keyword s/Any})

(defn- sha? [x] (= (count x) 40))

(s/defschema Fn (s/conditional fn? s/Any))

(s/defschema Sha (s/conditional sha? s/Str))

(s/defschema Semver {:major s/Int
                     :minor s/Int
                     :patch s/Int
                     :pre-release (s/maybe s/Str)
                     :build (s/maybe s/Str)})

(s/defschema Pull {:title s/Str
                   :sha Sha
                   s/Keyword s/Any})

(s/defschema Issue (s/pair s/Str 'name s/Str 'link))

(s/defschema Change {:type s/Str :scope s/Str :subject s/Str :issues [Issue]})

(s/defschema Tag {:name s/Str
                  :sha Sha
                  (s/optional-key :from) (s/maybe Sha)
                  (s/optional-key :version) Semver
                  (s/optional-key :commits) [Sha]
                  (s/optional-key :pulls) [Pull]
                  (s/optional-key :changes) [Change]})
