(ns hu.ssh.github-changelog.github
  (:require
    [hu.ssh.github-changelog.util :as util]
    [hu.ssh.github-changelog.schema :refer [Pull]]
    [tentacles.pulls :as pulls]
    [schema.core :as s]))

(s/set-fn-validation! true)

(def pull-sha (partial util/value-at [:head :sha]))

(s/defn fetch-pulls :- [Pull]
  [user :- s/Str
   repo :- s/Str
   {:keys [token]}]
  (map
    #(assoc % :sha  (pull-sha %))
    (pulls/pulls user repo (merge {:token token, :all-pages true, :state  "closed"}))))
