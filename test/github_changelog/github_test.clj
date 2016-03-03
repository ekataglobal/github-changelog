(ns github-changelog.github-test
  (:require
    [github-changelog.github :as github]
    [github-changelog.schema :refer [Config Pull]]
    [github-changelog.util :refer [gen-sha]]
    [clojure.test :refer :all]
    [clojure.set :refer [subset?]]
    [clj-http.fake :refer [with-fake-routes-in-isolation]]
    [cheshire.core :refer [generate-string]]
    [schema.core :as s]
    [schema.experimental.complete :as c]))

(def github-api "http://api.github.com/")
(def config (c/complete {:github-api github-api
                         :user       "raszi"
                         :repo       "changelog-test"} Config))

(def api-endpoint "http://api.github.com/repos/raszi/changelog-test/pulls")

(deftest pulls-url
  (is (= api-endpoint (github/pulls-url config))))

(deftest parse-pull
  (let [sha (gen-sha)
        pull (github/parse-pull {:title "Something" :body nil :head {:sha sha}})]
    (is (s/validate Pull pull))
    (is (= sha (:sha pull)))))

(defn- mocked-response-fn
  ([body] (mocked-response-fn body {}))
  ([body options]
   (let [body-str (generate-string body)]
     (fn [_req] (merge {:status 200 :headers {} :body body-str} options)))))

(deftest get-pulls
  (testing "without multiple pages"
    (let [body [{:example ""}]]
      (with-fake-routes-in-isolation
        {{:address api-endpoint :query-params {:state "closed"}} (mocked-response-fn body)}
        (is (= body (github/get-pulls config))))))

  (testing "with multiple pages"
    (let [first-body [{:example1 ""}]
          links {:last {:href "?page=2"}}
          second-body [{:example2 ""}]]
      (with-fake-routes-in-isolation
        {{:address api-endpoint :query-params {:state "closed"}} (mocked-response-fn first-body {:links links})
         {:address api-endpoint :query-params {:state "closed" :page "2"}} (mocked-response-fn second-body)}
        (is (subset? (into first-body second-body) (set (github/get-pulls config))))))))
