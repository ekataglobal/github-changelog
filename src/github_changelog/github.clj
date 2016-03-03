(ns github-changelog.github
  (:require
    [github-changelog.util :refer [strip-trailing extract-params]]
    [github-changelog.schema :refer [Config Pull]]
    [clojure.string :refer [split]]
    [clj-http.client :as http]
    [throttler.core :refer [throttle-fn]]
    [schema.core :as s]))

(defn parse-pull [pull]
  (assoc pull :sha (get-in pull [:head :sha])))

(defn pulls-url [config]
  (let [{:keys [github-api user repo]} config]
    (format "%s/repos/%s/%s/pulls" (strip-trailing github-api "/") user repo)))

(defn- make-request
  ([config] (make-request config {}))
  ([config params]
   (let [oauth-token (:token config)]
     {:as           :json
      :query-params (merge {:state "closed"} params)
      :headers      {"User-Agent"    "GitHub-Changelog"
                     "Authorization" (str "token " oauth-token)}})))

(defn- last-page-number [links]
  (some-> (get-in links [:last :href])
          (split #"\?")
          second
          extract-params
          :page
          Long/parseLong))

(defn- gen-pages [links]
  (if-let [last-page (last-page-number links)]
    (range 2 (inc last-page))
    []))

(defn- make-requests [config links]
  (map #(make-request config {:page %}) (gen-pages links)))

(defn- call-api-fn [config]
  (let [rate-limit (get config :rate-limit 5)
        end-point (pulls-url config)]
    (throttle-fn (partial http/get end-point) rate-limit :second)))

(s/defn get-pulls :- [Pull] [config :- Config]
  (let [call-api (call-api-fn config)
        request (make-request config)
        first-response (call-api request)
        links (:links first-response)
        first-body (:body first-response)
        requests (make-requests config links)
        rest-responses (pmap call-api requests)]
    (into first-body (flatten (map :body rest-responses)))))

(s/defn fetch-pulls :- [Pull]
  [config :- Config]
  (map parse-pull (get-pulls config)))
