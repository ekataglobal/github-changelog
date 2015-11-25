(ns hu.ssh.github-changelog.github
  (:require
    [hu.ssh.github-changelog.schema :refer [Config Pull]]
    [tentacles.core :refer [with-url]]
    [tentacles.pulls :as pulls]
    [schema.core :as s]))

(s/defn parse-pull :- Pull
  [pull]
  (assoc pull :sha (get-in pull [:head :sha])))

(s/defn fetch-pulls :- [Pull]
  [config :- Config]
  (let [{:keys [user repo token github-api]} config
        options {:token token, :all-pages true, :state "closed"}]
    (with-url github-api
              (map parse-pull (pulls/pulls user repo options)))))