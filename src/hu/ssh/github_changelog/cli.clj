(ns hu.ssh.github-changelog.cli
  (:require
    [clojure.tools.cli :as cli]
    [clojure.string :refer [join]]
    [hu.ssh.github-changelog.core :refer [changelog]]
    [hu.ssh.github-changelog.validators :refer [min-length url]])
  (:gen-class))

(defn- exit [status msg]
  (println msg)
  (System/exit status))

(defn- error-msg [errors]
  (let [prefixed-errors (map #(str "   " %) errors)]
    (join \newline ["The following errors occurred while parsing your command:" "" (join \newline prefixed-errors)])))

(def cli-options
  [["-o" "--token TOKEN" "Sets the OAuth token"
    :missing "Missing OAuth token"
    :validate (min-length 40)]

   ["-g" "--github URL" "Sets the GitHub URL"
    :validate (url)]

   ["-j" "--jira URL" "Sets the Jira URL"
    :validate (url)]

   ["-a" "--github-api URL" "Sets the Github API"
    :validate (url)]

   ["-d" "--debug" "Turn on debug mode"]
   ["-h" "--help"]])

(defn- usage [summary]
  (join
    \newline
    ["Usage: program-name [options...] <user/repo>"
     ""
     "Options:"
     summary]))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)
        [_ user repo] (re-find #"^(\w+)/(\w+)$" (str (first arguments)))]
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors))
      (not (and user repo)) (exit 2 (usage summary)))

    (changelog (merge {:user user :repo repo} options))))
