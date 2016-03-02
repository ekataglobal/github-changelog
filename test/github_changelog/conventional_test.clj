(ns github-changelog.conventional-test
  (:require
    [github-changelog.conventional :as conventional]
    [github-changelog.schema :refer [Config Pull Change]]
    [github-changelog.util :refer [gen-sha]]
    [clojure.test :refer :all]
    [clojure.string :refer [join]]
    [schema.experimental.complete :as c]
    [schema.core :as s]))

(def repo-url "https://github.company.com/user/repo")
(def jira-url "http://dev.clojure.org/jira")
(def config (c/complete {:jira jira-url} Config))

(deftest parse-issues
  (testing "with a JIRA issue"
    (let [pull (c/complete {:body "Fixes JIRA-1" :sha (gen-sha)} Pull)]
      (is (= [["JIRA-1" (str jira-url "/browse/JIRA-1")]] (conventional/parse-issues config pull)))))

  (testing "with a GitHub issue"
    (let [pull (c/complete {:body "Fixes #1" :sha (gen-sha) :base {:repo {:html_url repo-url}}} Pull)]
      (is (= [["#1" (str repo-url "/issues/1")]] (conventional/parse-issues config pull))))))

(defn- create-pull [title]
  (c/complete {:title title :sha (gen-sha)} Pull))

(deftest parse-pull
  (testing "with a correct formats"
    (are [title] (s/validate Change (conventional/parse-pull config (create-pull title)))
                 "feat(scope): enhance this and that"
                 "fix(scope): do not fail on invalid input"
                 "chore: clean up the codebase"))
  (testing "with invalid formats"
    (are [title] (nil? (conventional/parse-pull config (create-pull title)))
                 "this is just a PR"
                 "does not follow the rules"))
  (testing "with a full test"
    (let [pull (merge (create-pull "feat(the scope): subject line") {:body "Fixes #1, Closes JIRA-2"})
          change (conventional/parse-pull config pull)]
      (is (s/validate Change change))
      (is (= "feat" (:type change)))
      (is (= "the scope" (:scope change)))
      (is (= "subject line" (:subject change)))
      (is (= pull (:pull-request change)))
      (is (= 2 (count (:issues change)))))))
