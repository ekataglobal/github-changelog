(ns github-changelog.cli
  (:gen-class)
  (:require [clojure
             [edn :as edn]
             [string :as str]]
            [clojure.tools.cli :refer [parse-opts]]
            [github-changelog.core :refer [changelog]]
            [github-changelog.formatters.markdown :refer [format-tags]]))

(def cli-options
  [["-h" "--help"]])

(defn- join-lines [lines]
  (str/join \newline (flatten lines)))

(defn- exit [status msg]
  (when (seq msg)
    (println msg))
  (System/exit status))

(defn- error-msg [errors]
  (join-lines ["The following errors occurred while parsing your command:"
               ""
               errors]))

(defn- read-config [file]
  (edn/read-string (slurp file)))

(defn- generate [file]
  (->> (read-config file)
       changelog
       format-tags))

(defn- usage [options-summary]
  (join-lines ["Usage: program-name [options] <config.edn> ..."
               ""
               "Options:"
               options-summary]))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (empty? arguments) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))

    (doseq [config-file arguments]
      (println (generate config-file)))

    (exit 0 nil)))
