(ns hu.ssh.github-changelog.github
  (:require
    [hu.ssh.github-changelog.util :refer [strip-trailing extract-params]]
    [hu.ssh.github-changelog.schema :refer [Config Pull]]
    [clojure.string :refer [split]]
    [clj-http.client :as http]
    [throttler.core :refer [throttle-fn]]
    [schema.core :as s]))

(defn parse-pull [pull]
  (assoc pull :sha (get-in pull [:head :sha])))

(defn pulls-url [config]
  (let [{:keys [github-api user repo]} config]
    (format "%s/repos/%s/%s/pulls" (strip-trailing github-api "/") user repo)))

(defn- make-request [config params]
  (let [oauth-token (:token config)]
    {:as           :json
     :query-params (merge {:state "closed"} params)
     :headers      {"User-Agent"    "GitHub-Changelog"
                    "Authorization" (str "token " oauth-token)}}))

(def call-api (throttle-fn http/get 5 :second))

(defn- last-page-number [links]
  (-> (get-in links [:last :href])
      (split #"\?")
      second
      extract-params
      :page
      Integer/parseInt))

(defn- gen-pages [links]
  (range 2 (inc (last-page-number links))))

(defn- get-pulls [config]
  (let [end-point (pulls-url config)
        request (make-request config {})
        first-response (call-api end-point request)
        links (:_links first-response)
        first-body (:body first-response)]
    (if links
      (let [pages (gen-pages links)
            requests (map #(make-request config {:page %}) pages)
            rest-responses (pmap #(call-api end-point %) requests)]
        (into first-body (flatten (map :body rest-responses))))
      first-body)))

(s/defn fetch-pulls :- [Pull]
        [config :- Config]
  (->> (get-pulls config)
      (map parse-pull)))
