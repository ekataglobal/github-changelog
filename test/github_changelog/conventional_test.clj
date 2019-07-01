(ns github-changelog.conventional-test
  (:require [clojure
             [string :as str]
             [test :refer :all]]
            [github-changelog
             [conventional :as sut]
             [schema-generators :as g]]))

(def repo-url "https://github.company.com/user/repo")
(def jira-url "http://dev.clojure.org/jira/")
(def config (g/complete-config {:jira jira-url}))

(def jira-id "JIRA-1")
(def jira-issue-url (str jira-url "browse/" jira-id))

(defn generate-pull [body]
  (g/complete-pull {:body body :base {:repo {:html_url repo-url}}}))

(deftest parse-issue
  (testing "with a JIRA issue"
    (let [body (format "Fixes %s" jira-id)
          pull (generate-pull body)]
      (is (= [["JIRA-1" jira-issue-url]] (sut/parse-issues config pull)))))
  (testing "with a GitHub issue"
    (let [pull (generate-pull "Fixes #1")]
      (is (= [["#1" (str repo-url "/issues/1")]] (sut/parse-issues config pull))))))

(defn revert-pull [{:keys [user repo]} pull-id]
  (g/complete-revert-pull {:body (format "Reverts %s/%s#%d" user repo pull-id)}))

(deftest parse-issues
  (let [related (format "Related to [%s](%s)" jira-id jira-issue-url)
        fixes   (format "Fixes #1")
        body    (str/join '\n [related fixes])
        pull    (generate-pull body)]
    (is (= [[jira-id jira-issue-url]
             ["#1" "https://github.company.com/user/repo/issues/1"]] (sut/parse-issues config pull)))))

(deftest parse-pull
  (testing "with a revert"
    (are [pull-id] (= pull-id (:revert-pull (sut/parse-pull config (revert-pull config pull-id))))
      1
      2
      5))
  (testing "with a correct formats"
    (are [title] (not= nil (sut/parse-pull config (g/complete-pull {:title title})))
      "feat(scope): enhance this and that"
      "fix(scope): do not fail on invalid input"
      "chore: clean up the codebase"))
  (testing "with invalid formats"
    (are [title] (nil? (sut/parse-pull config (g/complete-pull {:title title})))
      "this is just a PR"
      "does not follow the rules"))
  (testing "with a full test"
    (let [pull (g/complete-pull {:title "feat(the scope): subject line" :body "Fixes #1, Closes JIRA-2"})
          change (sut/parse-pull config pull)]
      (is (= "feat" (:type change)))
      (is (= "the scope" (:scope change)))
      (is (= "subject line" (:subject change)))
      (is (= pull (:pull-request change)))
      (is (= 2 (count (:issues change)))))))

(def pulls (map #(g/complete-valid-pull {:number %}) (range 1 5)))

(defn revert [pulls]
  (->> pulls
       (map :number)
       (map #(assoc (revert-pull config %) :number (* 10 %)))))

(deftest parse-changes
  (are [pulls expected] (= expected (->> (g/complete-tag {:pulls pulls})
                                         (sut/parse-changes config)
                                         (:changes)
                                         (count)))
    pulls 4
    (concat (revert pulls) pulls) 0
    (concat (revert (drop 1 pulls)) pulls) 1
    (concat (revert (revert (take 1 pulls))) (revert pulls) pulls) 1))
