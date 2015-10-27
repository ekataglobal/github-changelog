(ns hu.ssh.github-changelog.github
  (:require
    [hu.ssh.github-changelog.util :as util]
    [hu.ssh.github-changelog.schema :refer [Config Pull]]
    [tentacles.core :refer [with-url]]
    [tentacles.pulls :as pulls]
    [schema.core :as s]))

(s/set-fn-validation! true)

(def pull-sha (partial util/value-at [:head :sha]))

(s/defn fetch-pulls :- [Pull]
  [config :- Config
   user :- s/Str
   repo :- s/Str]
  (with-url (:github-api config)
            (->> (pulls/pulls user repo (merge {:token (:token config), :all-pages true, :state "closed"}))
                 (map #(assoc % :sha (pull-sha %))))))
