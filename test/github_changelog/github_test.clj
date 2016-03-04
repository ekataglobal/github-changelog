(ns github-changelog.github-test
  (:require
    [github-changelog.github :as github]
    [github-changelog.schema :refer [Config Pull Sha]]
    [github-changelog.schema-generators :refer [generate sample]]
    [github-changelog.schema-complete :refer [complete]]
    [clojure.test :refer :all]
    [clojure.set :refer [subset?]]
    [clj-http.fake :refer [with-fake-routes-in-isolation]]
    [cheshire.core :refer [generate-string]]
    [schema.core :as s]))

(def github-api "http://api.github.com/")
(def config (complete {:github-api github-api
                         :user       "raszi"
                         :repo       "changelog-test"} Config))

(def api-endpoint "http://api.github.com/repos/raszi/changelog-test/pulls")

(deftest pulls-url
  (is (= api-endpoint (github/pulls-url config))))

(deftest parse-pull
  (let [sha (generate Sha)
        example-json {:number 1
                      :html_url ""
                      :title "Something"
                      :body nil
                      :head {:sha sha}
                      :base {:repo {:html_url ""}}}
        pull (github/parse-pull example-json)]
    (is (s/validate Pull pull))
    (is (= sha (:sha pull)))))

(defn- mocked-response-fn
  ([body] (mocked-response-fn body {}))
  ([body options]
   (let [body-str (generate-string body)]
     (fn [_req] (merge {:status 200 :headers {} :body body-str} options)))))

(deftest get-pulls
  (testing "without multiple pages"
    (let [body (sample 2 Pull)]
      (with-fake-routes-in-isolation
        {{:address api-endpoint :query-params {:state "closed"}} (mocked-response-fn body)}
        (is (= body (github/get-pulls config))))))

  (testing "with multiple pages"
    (let [first-body (sample 10 Pull)
          links {:last {:href "?page=2"}}
          second-body (sample 10 Pull)]
      (with-fake-routes-in-isolation
        {{:address api-endpoint :query-params {:state "closed"}} (mocked-response-fn first-body {:links links})
         {:address api-endpoint :query-params {:state "closed" :page "2"}} (mocked-response-fn second-body)}
        (let [merged (set (into first-body second-body))
              result (set (github/get-pulls config))]
          (is (= merged result)))))))
