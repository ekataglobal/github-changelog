(ns github-changelog.schema)

(defrecord Config [git github-api user repo])

(defrecord Semver [major minor patch])

(defrecord Pull [title number sha body html_url])

(defrecord Change [type scope subject pull-request issues])

(defrecord Tag [name sha])
