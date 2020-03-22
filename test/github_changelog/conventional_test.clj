(ns github-changelog.conventional-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]
            [clojure.test :refer [are deftest is testing]]
            [github-changelog.config :as config]
            [github-changelog.conventional :as sut]
            [github-changelog.core-spec :as core-spec]
            [github-changelog.github :as github]
            [github-changelog.spec :as spec]))

(def github-url "https://github.company.com/user/repo")
(def jira-url "http://dev.clojure.org/jira/")

(def config
  (-> (spec/sample ::config/config-map)
      (merge {:user   "user"
              :repo   "repo"
              :github github-url
              :jira   jira-url})))

(def jira-id "JIRA-1")
(def jira-issue-url (str jira-url "browse/" jira-id))

(defn sample-pull [overrides]
  (-> (spec/sample ::github/pull {::github/title sut/title-gen})
      (assoc-in [:base :repo :html_url] github-url)
      (merge overrides)))

(deftest parse-issue
  (testing "with a JIRA issue"
    (let [body (format "Fixes %s" jira-id)
          pull (sample-pull {:body body})]
      (is (= [["JIRA-1" jira-issue-url]] (sut/parse-issues config pull)))))
  (testing "with a GitHub issue"
    (let [pull (sample-pull {:body "Fixes #1"})]
      (is (= [["#1" (str github-url "/issues/1")]] (sut/parse-issues config pull))))))

(defn revert-pull [pull-id]
  (let [{:keys [user repo]} config]
    (sample-pull {:title "Revert ..."
                  :body  (format "Reverts %s/%s#%d" user repo pull-id)})))

(deftest parse-issues
  (let [related (format "Related to [%s](%s)" jira-id jira-issue-url)
        fixes   (format "Fixes #1")
        body    (str/join '\n [related fixes])
        pull    (sample-pull {:body body})]
    (is (= [[jira-id jira-issue-url]
            ["#1" "https://github.company.com/user/repo/issues/1"]] (sut/parse-issues config pull)))))

(deftest parse-pull
  (testing "with a revert"
    (are [pull-id] (= pull-id (:revert-pull (sut/parse-pull config (revert-pull pull-id))))
      1
      2
      5))
  (testing "with a correct formats"
    (are [title] (not= nil (sut/parse-pull config (sample-pull {:title title})))
      "feat(scope): enhance this and that"
      "fix(scope): do not fail on invalid input"
      "chore: clean up the codebase"))
  (testing "with invalid formats"
    (are [title] (nil? (sut/parse-pull config (sample-pull {:title title})))
      "this is just a PR"
      "does not follow the rules"))
  (testing "with a full test"
    (let [pull (sample-pull {:title "feat(the scope): subject line" :body "Fixes #1, Closes JIRA-2"})
          change (sut/parse-pull config pull)]
      (is (= "feat" (:type change)))
      (is (= "the scope" (:scope change)))
      (is (= "subject line" (:subject change)))
      (is (= pull (:pull-request change)))
      (is (= 2 (count (:issues change)))))))

(defn revert [pulls]
  (->> pulls
       (map :number)
       (map #(assoc (revert-pull %) :number (* 10 %)))))

(defn- sample-tag [pulls]
  (-> (spec/sample ::core-spec/tag)
      (assoc :pulls pulls)))

(deftest parse-changes
  (let [pulls (map #(sample-pull {:number %}) (range 1 5))]
    (are [related-pulls expected] (= expected (->> (sample-tag related-pulls)
                                                   (sut/parse-changes config)
                                                   (:changes)
                                                   (count)))
      pulls 4
      (concat (revert pulls) pulls) 0
      (concat (revert (drop 1 pulls)) pulls) 1
      (concat (revert (revert (take 1 pulls))) (revert pulls) pulls) 1)))
