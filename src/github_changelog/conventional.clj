(ns github-changelog.conventional
  (:require [clojure.string :refer [join starts-with?]]
            [github-changelog.util :refer [strip-trailing]]))

; https://help.github.com/articles/closing-issues-via-commit-messages/
(def close-keywords ["close" "closes" "closed" "fix" "fixes" "fixed" "resolve" "resolves" "resolved"])

(defn fixes-pattern
  ([pattern] (fixes-pattern pattern close-keywords))
  ([pattern closing-words]
   (re-pattern
    (format "(?i:%s) %s"
            (join \| closing-words)
            pattern))))

(def angular-pattern #"^(\w*)(?:\((.*)\))?\: (.*)$")

(defn collect-issues [pull pattern link-fn]
  (->> (re-seq pattern (str (:body pull)))
       (map second)
       (map #(vector % (link-fn %)))))

(def jira-pattern (fixes-pattern "\\[?([A-Z]+-\\d+)\\]?"))

(defn jira-issues [{:keys [jira]} pull]
  (when (seq jira)
    (let [base (str (strip-trailing jira) "/browse/")]
      (collect-issues pull jira-pattern (partial str base)))))

(def github-pattern (fixes-pattern "(#\\d+)"))

(defn- parse-int [x] (Integer. (re-find #"[0-9]+" x)))

(defn github-issues [_ pull]
  (let [base (str (get-in pull [:base :repo :html_url]) "/issues/")]
    (collect-issues pull github-pattern #(str base (parse-int %)))))

(defn parse-issues [config pull]
  (apply concat ((juxt jira-issues github-issues) config pull)))

(defn parse-revert [{:keys [user repo]} {:keys [title body]}]
  (if (starts-with? title "Revert ")
    (let [revert-prefix (format "Reverts %s/%s#" user repo)
          [prefix pull-id] (map join (split-at (count revert-prefix) body))]
      (if (starts-with? prefix revert-prefix)
        (parse-int pull-id)))))

(defn parse-pull [config {:keys [title] :as pull}]
  (if-let [pull-id (parse-revert config pull)]
    {:revert-pull pull-id
     :pull-request pull}
    (if-let [[_ type scope subject] (re-find angular-pattern title)]
      {:type type
       :scope scope
       :subject subject
       :pull-request pull
       :issues (parse-issues config pull)})))

(defn parse-changes [config {:keys [pulls] :as tag}]
  (->> (map (partial parse-pull config) pulls)
       (remove nil?)
       (assoc tag :changes)))
