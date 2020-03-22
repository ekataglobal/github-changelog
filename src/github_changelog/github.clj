(ns github-changelog.github
  (:require [clj-http.lite.client :as http]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [github-changelog.config :as config]
            [github-changelog.spec :as spec]
            [github-changelog.util :as util]
            [jsonista.core :as j]
            [throttler.core :as throttler])
  (:import com.fasterxml.jackson.databind.ObjectMapper))

(s/def ::head (s/keys :req-un [::spec/sha]))
(s/def ::number pos-int?)
(s/def ::title string?)
(s/def ::body string?)

(s/def ::pull (s/keys :req-un [::head ::number ::title ::body]))

(defn get-sha [pull]
  (get-in pull [:head :sha]))

(s/fdef get-sha
  :args (s/cat :pull ::pull)
  :ret ::spec/sha)

(defn pulls-url [{:keys [github-api user repo]
                  :or   {github-api (:github-api config/defaults)}}]
  (format "%s/repos/%s/%s/pulls" (util/strip-trailing github-api) user repo))

(defn headers [token]
  (cond-> {"User-Agent" "GitHub-Changelog"}
    token (assoc "Authorization" (str "token " token))))

(defn make-request
  ([config] (make-request config {}))
  ([{oauth-token :token} params]
   {:query-params (merge {:state "closed"} params)
    :headers      (headers oauth-token)}))

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

(def ^ObjectMapper mapper
  (j/object-mapper {:decode-key-fn true}))

(defn parse-json [str]
  (j/read-value str mapper))

(defn- issue-request [endpoint request]
  (update (http/get endpoint request) :body parse-json))

(defn- call-api-fn [config]
  (let [ratelimit (get config :rate-limit 5)
        endpoint  (pulls-url config)]
    (throttler/throttle-fn (partial issue-request endpoint) ratelimit :second)))

(defn fetch-pulls [config]
  (let [call-api                        (call-api-fn config)
        first-request                   (make-request config)
        first-response                  (call-api first-request)
        {links :links first-body :body} first-response
        rest-requests                   (make-requests config links)
        rest-responses                  (pmap call-api rest-requests)]
    (into first-body (flatten (map :body rest-responses)))))
