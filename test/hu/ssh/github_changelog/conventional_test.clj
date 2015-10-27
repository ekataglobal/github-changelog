(ns hu.ssh.github-changelog.conventional-test
  (:require
    [hu.ssh.github-changelog.conventional :as conventional]
    [hu.ssh.github-changelog.schema :refer [Config Pull]]
    [clojure.test :refer :all]
    [schema.experimental.complete :as c]))

(def repo-url "https://github.company.com/user/repo")
(def jira-url "http://dev.clojure.org/jira")
(def config (c/complete {:jira jira-url} Config))
(defn gen-sha [] (apply str (repeatedly 40 #(rand-nth "0123456789ABCDEF"))))

(deftest parse-issues
  (testing "with a JIRA issue"
    (let [pull (c/complete {:body "Fixes JIRA-1" :sha (gen-sha)} Pull)]
      (is (= [["JIRA-1" (str jira-url "/browse/JIRA-1")]] (conventional/parse-issues config pull)))))

  (testing "with a GitHub issue"
    (let [pull (c/complete {:body "Fixes #1" :sha (gen-sha) :base {:repo {:html_url repo-url}}} Pull)]
      (is (= [["1" (str repo-url "/issues/1")]] (conventional/parse-issues config pull))))))
