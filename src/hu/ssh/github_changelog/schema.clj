(ns hu.ssh.github-changelog.schema
  (:require [schema.core :as s]))

(s/defschema Config {:git s/Str
                     :jira (s/maybe s/Str)
                     :github-api s/Str
                     s/Keyword s/Any})

(s/defschema Fn (s/conditional ifn? s/Any))

(defn- sha? [x] (= (count x) 40))

(s/defschema Sha (s/conditional sha? s/Str))

(defn- natural? [x] (>= x 0))

(s/defschema Natural (s/conditional natural? s/Int))

(s/defschema Semver {:major Natural
                     :minor Natural
                     :patch Natural
                     :pre-release (s/maybe s/Str)
                     :build (s/maybe s/Str)})

(s/defschema Pull {:title s/Str
                   :sha Sha
                   :body (s/maybe s/Str)
                   s/Keyword s/Any})

(s/defschema Issue (s/pair s/Str 'name s/Str 'link))

(def ChangeType s/Str)

(s/defschema Change {:type ChangeType
                     :scope (s/maybe s/Str)
                     :subject s/Str
                     :pull-request Pull
                     :issues [Issue]})

(s/defschema Tag {:name s/Str
                  :sha Sha
                  (s/optional-key :from) (s/maybe Sha)
                  (s/optional-key :version) (s/maybe Semver)
                  (s/optional-key :commits) [Sha]
                  (s/optional-key :pulls) [Pull]
                  (s/optional-key :changes) [Change]})
