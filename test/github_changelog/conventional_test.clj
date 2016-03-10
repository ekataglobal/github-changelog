(ns github-changelog.conventional-test
  (:require
   [github-changelog
    [conventional :as conventional]
    [schema-generators :refer [complete-pull complete-config]]]
   [clojure.test :refer :all]
   [clojure.test.check.generators :as gen]
   [clojure.string :refer [join]]))

(def repo-url "https://github.company.com/user/repo")
(def jira-url "http://dev.clojure.org/jira")
(def config (complete-config {:jira jira-url}))

(deftest parse-issue
  (testing "with a JIRA issue"
    (let [pull (complete-pull {:body "Fixes JIRA-1"})]
      (is (= [["JIRA-1" (str jira-url "/browse/JIRA-1")]] (conventional/parse-issues config pull)))))
  (testing "with a GitHub issue"
    (let [pull (complete-pull {:body "Fixes #1" :base {:repo {:html_url repo-url}}})]
      (is (= [["#1" (str repo-url "/issues/1")]] (conventional/parse-issues config pull))))))

(conventional/parse-pull config (complete-pull {:title "this is just a PR"}))

(deftest parse-pull
  (testing "with a correct formats"
    (are [title] (not= nil (conventional/parse-pull config (complete-pull {:title title})))
                 "feat(scope): enhance this and that"
                 "fix(scope): do not fail on invalid input"
                 "chore: clean up the codebase"))
  (testing "with invalid formats"
    (are [title] (nil? (conventional/parse-pull config (complete-pull {:title title})))
                 "this is just a PR"
                 "does not follow the rules"))
  (testing "with a full test"
    (let [pull (complete-pull {:title "feat(the scope): subject line" :body "Fixes #1, Closes JIRA-2"})
          change (conventional/parse-pull config pull)]
      (is (= "feat" (:type change)))
      (is (= "the scope" (:scope change)))
      (is (= "subject line" (:subject change)))
      (is (= pull (:pull-request change)))
      (is (= 2 (count (:issues change)))))))
