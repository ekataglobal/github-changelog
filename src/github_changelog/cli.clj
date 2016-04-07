(ns github-changelog.cli
  (:require
   [github-changelog.core :refer [changelog]]
   [github-changelog.validators :refer [min-length url]]
   [github-changelog.formatters.markdown :refer [format-tags]]
   [clojure.edn :as edn]
   [clojure.string :refer [join]])
  (:gen-class))

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
