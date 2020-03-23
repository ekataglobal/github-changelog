(ns github-changelog.conventional
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]
            [github-changelog.config :as config]
            [github-changelog.core-spec :as core-spec]
            [github-changelog.github :as github]
            [github-changelog.spec :as spec]
            [github-changelog.util :refer [strip-trailing]]))

; https://help.github.com/articles/closing-issues-via-commit-messages/
(def close-keywords #{"close" "closes" "closed" "fix" "fixes" "fixed" "resolve" "resolves" "resolved" "related to" "relates to"})

(def angular-pattern #"^(\w*)(?:\((.*)\))?\: (.*)$")

(s/def ::type ::spec/non-blank-string)
(s/def ::scope string?)
(s/def ::subject ::spec/non-blank-string)

(s/def ::issue (s/tuple ::spec/non-blank-string  ::spec/url))

(s/def ::issues (s/* ::issue))

(defn title-gen []
  (gen/fmap (fn [[type scope subject]]
              (if (str/blank? scope)
                (format "%s: %s" type subject)
                (format "%s(%s): %s" type scope subject)))
            (s/gen (s/tuple ::type ::scope ::subject))))

(s/def ::title
  (s/with-gen
    (s/and string? #(re-matches angular-pattern %))
    title-gen))

;; (s/def ::revert-pull pos-int?)

;; (s/def ::revert-pr
;;   (s/keys :req-un [::revert-pull ::pull-request]))

(s/def ::pull-request ::github/pull)

(s/def ::change (s/keys :req-un [::type ::scope ::subject ::pull-request ::issues]))
(s/def ::changes (s/* ::change))

(s/def ::tag-with-changes
  (s/merge ::core-spec/tag-with-pulls (s/keys :req-un [::changes])))

(defn fixes-pattern
  ([pattern] (fixes-pattern pattern close-keywords))
  ([pattern closing-words]
   (re-pattern
    (format "(?i:%s) %s"
            (str/join \| closing-words)
            pattern))))

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
  (when (str/starts-with? title "Revert ")
    (let [revert-prefix    (format "Reverts %s/%s#" user repo)
          [prefix pull-id] (map str/join (split-at (count revert-prefix) body))]
      (when (str/starts-with? prefix revert-prefix)
        (parse-int pull-id)))))

(defn parse-pull [config {:keys [title] :as pull}]
  (if-let [pull-id (parse-revert config pull)]
    {:revert-pull  pull-id
     :pull-request pull}
    ;; TODO - when-let?
    (if-let [[_ type scope subject] (re-find angular-pattern title)]
      {:type         type
       :scope        scope
       :subject      subject
       :pull-request pull
       :issues       (parse-issues config pull)})))

(defn reverted-ids [pulls]
  (->> (map :revert-pull pulls)
       (remove nil?)
       (set)))

(defn filter-reverted [pulls pull]
  (let [reverted-pulls (reverted-ids pulls)
        pull-id        (get-in pull [:pull-request :number])]
    (if (reverted-pulls pull-id)
      pulls
      (conj pulls pull))))

(defn parse-changes [config {:keys [pulls] :as tag}]
  (->> (map (partial parse-pull config) pulls)
       (remove nil?)
       (reduce filter-reverted [])
       (remove :revert-pull)
       (assoc tag :changes)))

(s/fdef parse-changes
  :args (s/cat :config ::config/config-map :tag ::core-spec/tag-with-pulls)
  :ret (s/* ::tag-with-changes))

(comment
  (s/exercise-fn `parse-changes)
  (s/exercise ::issue)
  (s/exercise ::issues)
  (s/exercise ::title))
