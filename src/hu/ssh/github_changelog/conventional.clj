(ns hu.ssh.github-changelog.conventional
  (:require [clojure.string :as string]))

(defn- fixes-pattern [pattern]
  (let [close-keywords ["close" "closes" "closed" "fix" "fixes" "fixed" "resolve" "resolves" "resolved"]]
    (re-pattern (format "(?i:%s) %s" (string/join \| close-keywords) pattern))))

(def header-pattern #"^(\w*)(?:\((.*)\))?\: (.*)$")

(defn- collect-issues [pull base pattern]
  (let [body (if-let [body (get pull :body "")] body "")]
    (->> (re-seq (fixes-pattern pattern) body)
         (map second)
         (map #(vector % (str base %))))))

(defn- jira-issues [config pull]
  (let [base (str (:jira config) "/browse/")]
    (collect-issues pull base "([A-Z]+-\\d+)")))

(defn- github-issues [_config pull]
  (let [base (str (get-in pull [:base :repo :html_url]) "/issues/")]
    (collect-issues pull base "#(\\d+)")))

(defn- parse-issues [config pull]
  (apply concat ((juxt jira-issues github-issues) config pull)))

(defn- parse-body [config pull]
  {:pre [(:base pull)]}
  (assoc pull :jira-issues (jira-issues config pull)))

(defn- parse-pull [config pull]
  {:pre  [(:title pull)]
   :post [(every? % [:type :scope :subject])]}
  (let [[_ type scope subject] (re-find header-pattern (:title pull))]
    {:type type :scope scope :subject subject :issues (parse-issues config pull)}))

(defn parse-changes [config tag]
  {:pre  [(:pulls tag)]
   :post [(:changes %)]}
  (assoc tag :changes (map parse-pull config (:pulls tag))))
