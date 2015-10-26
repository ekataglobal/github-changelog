(ns hu.ssh.github-changelog.conventional
  (:require
    [hu.ssh.github-changelog.util :as util]
    [clojure.string :as string]))

; https://help.github.com/articles/closing-issues-via-commit-messages/
(defn- fixes-pattern [pattern]
  (let [close-keywords ["close" "closes" "closed" "fix" "fixes" "fixed" "resolve" "resolves" "resolved"]]
    (re-pattern (format "(?i:%s) %s" (string/join \| close-keywords) pattern))))

(def header-pattern #"^(\w*)(?:\((.*)\))?\: (.*)$")

(defn- collect-issues [pull pattern link-fn]
  (->> (re-seq (fixes-pattern pattern) (str (:body pull)))
       (map second)
       (map #(vector % (link-fn %)))))

(defn- jira-issues [config pull]
  (let [base (str (:jira config) "/browse/")]
    (collect-issues pull "([A-Z]+-\\d+)" (util/prepend base))))

(defn- github-issues [_config pull]
  (let [base (str (get-in pull [:base :repo :html_url]) "/issues/")]
    (collect-issues pull "#(\\d+)" (util/prepend base))))

(defn- parse-issues [config pull]
  (apply concat ((juxt jira-issues github-issues) config pull)))

(defn- parse-pull [config pull]
  {:pre  [(:title pull)]
   :post [(every? % [:type :scope :subject])]}
  (let [[_ type scope subject] (re-find header-pattern (:title pull))]
    {:type type :scope scope :subject subject :issues (parse-issues config pull)}))

(defn parse-changes [config tag]
  {:pre  [(:pulls tag)]
   :post [(:changes %)]}
  (assoc tag :changes (map parse-pull config (:pulls tag))))
