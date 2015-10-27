(ns hu.ssh.github-changelog.util)

(def git-url (partial format "%s/%s/%s.git"))

(defn str-map [f & sqs] (apply str (apply map f sqs)))

(defn value-at
  [ks m]
  {:pre  [(map? m) (vector? ks)]
   :post [(string? %)]}
  (get-in m ks))
