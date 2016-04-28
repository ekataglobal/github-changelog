(ns github-changelog.cli
  (:gen-class)
  (:require [clojure.edn :as edn]
            [github-changelog.core :refer [changelog]]
            [github-changelog.formatters.markdown :refer [format-tags]]))

(defn- exit [status msg]
  (when msg
    (println msg))
  (System/exit status))

(defn- read-config [file]
  (edn/read-string (slurp file)))

(defn- generate [file]
  (->> (read-config file)
       changelog
       format-tags))

(defn -main [& [config-file]]
  (when (empty? config-file)
    (exit 1 "Usage: program-name config-file.edn"))

  (exit 0 (generate config-file)))
