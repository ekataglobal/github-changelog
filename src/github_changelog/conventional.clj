(ns github-changelog.conventional
  (:require [clojure.string :as str]
            [github-changelog.util :refer [strip-trailing]]))

; https://help.github.com/articles/closing-issues-via-commit-messages/
(def close-keywords #{"close" "closes" "closed" "fix" "fixes" "fixed" "resolve" "resolves" "resolved" "related to" "relates to"})

(defn fixes-pattern
  ([pattern] (fixes-pattern pattern close-keywords))
  ([pattern closing-words]
   (re-pattern
    (format "(?i:%s) %s"
            (str/join \| closing-words)
            pattern))))

(def angular-pattern #"^(\w*)(?:\((.*)\))?\: (.*)$")

(defn collect-issues [pull pattern link-fn]
  (->> (:body pull)
       (str)
       (re-seq pattern)
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
  (if (str/starts-with? title "Revert ")
    (let [revert-prefix (format "Reverts %s/%s#" user repo)
          [prefix pull-id] (map str/join (split-at (count revert-prefix) body))]
      (if (str/starts-with? prefix revert-prefix)
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

(defn reverted-ids [pulls]
  (->> (map :revert-pull pulls)
       (remove nil?)
       (set)))

(defn filter-reverted [pulls {:keys [revert-pull pull-request] :as pull}]
  (let [reverted-pulls (reverted-ids pulls)
        pull-id (get-in pull [:pull-request :number])]
    (if (reverted-pulls pull-id)
      pulls
      (conj pulls pull))))

(defn parse-changes [config {:keys [pulls] :as tag}]
  (->> (map (partial parse-pull config) pulls)
       (remove nil?)
       (reduce filter-reverted [])
       (remove :revert-pull)
       (assoc tag :changes)))
