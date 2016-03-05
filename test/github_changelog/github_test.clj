(ns github-changelog.github-test
  (:require
    [github-changelog.github :as github]
    [github-changelog.schema :as schema]
    [github-changelog.schema-generators :refer [generate sample]]
    [github-changelog.schema-complete :refer [complete]]
    [clojure.test :refer :all]
    [clj-http.fake :refer [with-fake-routes-in-isolation]]
    [cheshire.core :refer [generate-string]]
    [schema.core :refer [validate]]))

(def github-api "http://api.github.com/")
(def config (complete {:github-api github-api
                         :user       "raszi"
                         :repo       "changelog-test"} schema/Config))

(def api-endpoint "http://api.github.com/repos/raszi/changelog-test/pulls")

(defn- sample-pull
  ([] (sample-pull (generate schema/Sha)))
  ([sha]
   {:number   (generate schema/Natural)
    :html_url ""
    :title    "Something"
    :body     nil
    :head     {:sha sha}
    :base     {:repo {:html_url ""}}}))

(deftest pulls-url
  (is (= api-endpoint (github/pulls-url config))))

(deftest parse-pull
  (let [sha (generate schema/Sha)
        pull (github/parse-pull (sample-pull sha))]
    (is (validate schema/Pull pull))
    (is (= sha (:sha pull)))))

(defn- mocked-response-fn
  ([body] (mocked-response-fn body {}))
  ([body options]
   (let [body-str (generate-string body)]
     (fn [_req] (merge {:status 200 :headers {} :body body-str} options)))))

(deftest fetch-pulls
  (testing "without multiple pages"
    (let [body [(sample-pull)]]
      (with-fake-routes-in-isolation
        {{:address api-endpoint :query-params {:state "closed"}} (mocked-response-fn body)}
        (let [result (github/fetch-pulls config)]
          (is (= 1 (count result)))
          (is (validate [schema/Pull] result))))))

  (testing "with multiple pages"
    (let [first-body (repeatedly 10 sample-pull)
          links {:last {:href "?page=2"}}
          second-body (repeatedly 10 sample-pull)]
      (with-fake-routes-in-isolation
        {{:address api-endpoint :query-params {:state "closed"}} (mocked-response-fn first-body {:links links})
         {:address api-endpoint :query-params {:state "closed" :page "2"}} (mocked-response-fn second-body)}
        (let [result (github/fetch-pulls config)]
          (is (= 20 (count result)))
          (is (validate [schema/Pull] result)))))))
