(ns hu.ssh.github-changelog.conventional
  (:require
    [hu.ssh.github-changelog.util :as util]
    [hu.ssh.github-changelog.schema :refer [Config Tag Issue Pull Change Fn]]
    [schema.core :as s]
    [clojure.string :as string]))

(s/set-fn-validation! true)

; https://help.github.com/articles/closing-issues-via-commit-messages/
(s/defn fixes-pattern :- s/Regex
  [pattern :- s/Str]
  (let [close-keywords ["close" "closes" "closed" "fix" "fixes" "fixed" "resolve" "resolves" "resolved"]]
    (re-pattern (format "(?i:%s) %s" (string/join \| close-keywords) pattern))))

(def header-pattern #"^(\w*)(?:\((.*)\))?\: (.*)$")

(s/defn collect-issues :- [Issue]
  [pull :- Pull
   pattern :- s/Str
   link-fn :- Fn]
  (->> (re-seq (fixes-pattern pattern) (str (:body pull)))
       (map second)
       (map #(vector % (link-fn %)))))

(s/defn jira-issues :- [Issue]
  [config :- Config
   pull :- Pull]
  (let [base (str (:jira config) "/browse/")]
    (collect-issues pull "([A-Z]+-\\d+)" (util/prepend base))))

(s/defn github-issues :- [Issue]
  [_config :- Config
   pull :- Pull]
  (let [base (str (get-in pull [:base :repo :html_url]) "/issues/")]
    (collect-issues pull "#(\\d+)" (util/prepend base))))

(s/defn parse-issues :- [Issue]
  [config :- Config
   pull :- Pull]
  (apply concat ((juxt jira-issues github-issues) config pull)))

(s/defn parse-pull :- (s/maybe Change)
  [config :- Config
   pull :- Pull]
  (if-let [[_ type scope subject] (re-find header-pattern (:title pull))]
    {:type type
     :scope scope
     :subject subject
     :pull-request pull
     :issues (parse-issues config pull)}))

(s/defn parse-changes :- Tag
  [config :- Config
   tag :- Tag]
  (->> (:pulls tag)
       (map (partial parse-pull config))
       (remove nil?)
       (assoc tag :changes)))
