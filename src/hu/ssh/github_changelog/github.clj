(ns hu.ssh.github-changelog.github
  (:require
    [hu.ssh.github-changelog.util :as util]
    [tentacles.pulls :as pulls]))

(def pull-sha (partial util/value-at [:head :sha]))

(defn fetch-pulls [user repo {:keys [token]}]
  {:pre  [(string? user) (string? repo)]
   :post [(seq? %)]}
  (map
    #(assoc % :sha  (pull-sha %))
    (pulls/pulls user repo (merge {:token token, :all-pages true, :state  "closed"}))))
