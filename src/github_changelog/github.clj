(ns github-changelog.github
  (:require [clj-http.lite.client :as http]
            [clojure.string :as str]
            [github-changelog
             [defaults :as defaults]
             [util :as util]]
            [jsonista.core :as j]
            [throttler.core :as throttler]))

(defn parse-pull [pull]
  (assoc pull :sha (get-in pull [:head :sha])))

(defn pulls-url [{:keys [github-api user repo]
                  :or   {github-api (:github-api defaults/config)}}]
  (format "%s/repos/%s/%s/pulls" (util/strip-trailing github-api) user repo))

(defn- make-request
  ([config] (make-request config {}))
  ([{oauth-token :token} params]
   {:as           :json
    :query-params (merge {:state "closed"} params)
    :headers      {"User-Agent"    "GitHub-Changelog"
                   "Authorization" (str "token " oauth-token)}}))

(defn- last-page-number [links]
  (some-> (get-in links [:last :href])
          (str/split #"\?")
          second
          util/extract-params
          :page
          Long/parseLong))

(defn- gen-pages [links]
  (when-let [last-page (last-page-number links)]
    (range 2 (inc last-page))))

(defn- make-requests [config links]
  (map #(make-request config {:page %}) (gen-pages links)))

(defn- issue-request-fn [endpoint]
  (fn [request]
    (let [response (http/get endpoint request)]
      (update response :body j/read-value))))

(defn- call-api-fn [config]
  (let [rate-limit (get config :rate-limit 5)
        end-point (pulls-url config)]
    (throttler/throttle-fn (issue-request-fn end-point) rate-limit :second)))

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
