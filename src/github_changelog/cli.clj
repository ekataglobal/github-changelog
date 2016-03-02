(ns github-changelog.cli
  (:require
    [github-changelog.core :refer [changelog]]
    [github-changelog.validators :refer [min-length url]]
    [github-changelog.formatters.markdown :refer [format-tags]]
    [clojure.tools.cli :as cli]
    [clojure.edn :as edn]
    [clojure.string :refer [join]])
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
        default-config (edn/read-string (slurp "resources/config.edn"))
        [_ user repo] (re-find #"^([\w\-]+)/([\w\-]+)$" (str (first arguments)))]
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors))
      (not (and user repo)) (exit 2 (usage summary)))

    (->> (merge default-config options {:user user :repo repo})
         changelog
         format-tags
         (exit 0))))
