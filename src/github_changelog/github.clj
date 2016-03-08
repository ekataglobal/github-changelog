(ns github-changelog.github
  (:require
    [github-changelog.util :refer [strip-trailing extract-params]]
    [clojure.string :refer [split]]
    [clj-http.client :as http]
    [throttler.core :refer [throttle-fn]]))

(defn parse-pull [pull]
  (assoc pull :sha (get-in pull [:head :sha])))

(defn pulls-url [{:keys [github-api user repo]}]
  (format "%s/repos/%s/%s/pulls" (strip-trailing github-api "/") user repo))

(defn- make-request
  ([config] (make-request config {}))
  ([{oauth-token :token} params]
   {:as           :json
    :query-params (merge {:state "closed"} params)
    :headers      {"User-Agent"    "GitHub-Changelog"
                   "Authorization" (str "token " oauth-token)}}))

(defn- last-page-number [links]
  (some-> (get-in links [:last :href])
          (split #"\?")
          second
          extract-params
          :page
          Long/parseLong))

(defn- gen-pages [links]
  (when-let [last-page (last-page-number links)]
    (range 2 (inc last-page))))

(defn- make-requests [config links]
  (map #(make-request config {:page %}) (gen-pages links)))

(defn- call-api-fn [config]
  (let [rate-limit (get config :rate-limit 5)
        end-point (pulls-url config)]
    (throttle-fn (partial http/get end-point) rate-limit :second)))

(defn- get-pulls [config]
  (let [call-api (call-api-fn config)
        first-request (make-request config)
        first-response (call-api first-request)
        {links :links first-body :body} first-response
        rest-requests (make-requests config links)
        rest-responses (pmap call-api rest-requests)]
    (into first-body (flatten (map :body rest-responses)))))

(defn fetch-pulls [config]
  (map parse-pull (get-pulls config)))
