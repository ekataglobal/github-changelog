(ns github-changelog.github-test
  (:require
    [github-changelog.github :as github]
    [clojure.test.check.generators :as gen]
    [github-changelog.schema-generators :as sgen]
    [clojure.test :refer :all]
    [clj-http.fake :refer [with-fake-routes-in-isolation]]
    [cheshire.core :refer [generate-string]]))

(def github-api "http://api.github.com/")
(def config (sgen/complete-config {:github-api github-api
                                   :user       "raszi"
                                   :repo       "changelog-test"}))

(def api-endpoint "http://api.github.com/repos/raszi/changelog-test/pulls")

(defn- sample-pull
  ([] (sample-pull (gen/generate sgen/sha)))
  ([sha]
   (sgen/complete-pull
    {:head     {:sha sha}
     :base     {:repo {:html_url ""}}})))

(deftest pulls-url
  (is (= api-endpoint (github/pulls-url config))))

(deftest parse-pull
  (let [sha (gen/generate sgen/sha)
        pull (github/parse-pull (sample-pull sha))]
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
          (is (= 1 (count result)))))))

  (testing "with multiple pages"
    (let [first-body (repeatedly 10 sample-pull)
          links {:last {:href "?page=2"}}
          second-body (repeatedly 10 sample-pull)]
      (with-fake-routes-in-isolation
        {{:address api-endpoint :query-params {:state "closed"}} (mocked-response-fn first-body {:links links})
         {:address api-endpoint :query-params {:state "closed" :page "2"}} (mocked-response-fn second-body)}
        (let [result (github/fetch-pulls config)]
          (is (= 20 (count result))))))))
