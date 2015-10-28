(ns hu.ssh.github-changelog.github
  (:require
    [hu.ssh.github-changelog.schema :refer [Config Pull]]
    [tentacles.core :refer [with-url]]
    [tentacles.pulls :as pulls]
    [schema.core :as s]))

(s/set-fn-validation! true)

(s/defn parse-pull :- Pull
  [pull]
  (assoc pull :sha (get-in pull [:head :sha])))

(s/defn fetch-pulls :- [Pull]
  [config :- Config
   user :- s/Str
   repo :- s/Str]
  (let [options {:token (:token config), :all-pages true, :state "closed"}]
    (with-url (:github-api config)
              (map parse-pull (pulls/pulls user repo options)))))
