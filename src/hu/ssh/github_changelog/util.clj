(ns hu.ssh.github-changelog.util)

(def git-url (partial format "%s/%s/%s.git"))

(defn prepend [s] (partial str s))

(defn value-at
  [ks m]
  {:pre  [(map? m) (vector? ks)]
   :post [(string? %)]}
  (get-in m ks))
