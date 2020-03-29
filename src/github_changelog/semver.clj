(ns github-changelog.semver
  (:require [clj-semver.core :as semver]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]
            [github-changelog.spec :as spec]))

(defn- map->str [{:keys [major minor patch pre-release build]}]
  (cond-> (str/join \. [major minor patch])
    pre-release (str "-" pre-release)
    build (str "+" build)))

(def nat-int (s/with-gen
               nat-int?
               #(gen/int)))

(s/def ::major nat-int)
(s/def ::minor nat-int)
(s/def ::patch nat-int)

(s/def ::pre-release (s/with-gen
                       (s/nilable ::spec/non-blank-string)
                       gen/string-alphanumeric))

(s/def ::build (s/with-gen
                 (s/nilable ::spec/non-blank-string)
                 gen/string-alphanumeric))

(s/def ::version
  (s/keys :req-un [::major ::minor ::patch]
          :opt-un [::pre-release ::build]))

(s/def ::version-string
  (s/with-gen
    semver/valid-format?
    #(gen/fmap map->str (s/gen ::version))))

(s/def ::version-or-string (s/or :version ::version
                                 :string ::version-string))

(s/def ::types #{:major :minor :patch :pre-release :build})

(def newer? semver/newer?)

(s/fdef newer?
  :args (s/cat :a ::version-or-string :b ::version-or-string)
  :ret boolean?)

(def valid? semver/valid?)

(s/fdef valid?
  :args (s/cat :v any?)
  :ret boolean?)

(defn parse [version]
  (try (semver/parse version)
       (catch java.lang.AssertionError _e nil)))

(s/fdef parse
  :args (s/cat :version ::version)
  :ret (s/nilable ::version)
  :fn (s/or
       :invalid #(nil? (:ret %))
       :valid #(= (-> % :args :version) (map->str (:ret %)))))

(defn- strip-prefix [tag-name prefix]
  (if (str/starts-with? tag-name prefix)
    (subs tag-name (count prefix))
    tag-name))

(defn extract [tag-name prefix]
  (-> (strip-prefix tag-name prefix) parse))

(s/fdef extract
  :args (s/cat :tag-name string? :prefix string?)
  :ret (s/nilable ::version)
  :fn (s/or
       :invalid #(nil? (:ret %))
       :valid #(= (-> % :args :tag-name) (str (-> % :args :prefix) (map->str (:ret %))))))

(defn get-type [{:keys [minor patch pre-release build]}]
  (cond
    (and (zero? minor) (zero? patch) (str/blank? pre-release) (str/blank? build)) :major
    (and (zero? patch) (str/blank? pre-release) (str/blank? build)) :minor
    (and (str/blank? pre-release) (str/blank? build)) :patch
    (and (str/blank? build)) :pre-release
    :else :build))

(s/fdef get-type
  :args (s/cat :version ::version)
  :ret ::types)

(defn major? [version] (= :major (get-type version)))
(defn minor? [version] (= :minor (get-type version)))
(defn patch? [version] (= :patch (get-type version)))
(defn pre-release? [version] (= :pre-release (get-type version)))
(defn build? [version] (= :build (get-type version)))

(def overrides
  {::major       #(s/gen pos-int?)
   ::minor       #(gen/return 0)
   ::patch       #(gen/return 0)
   ::pre-release #(gen/return nil)
   ::build       #(gen/return nil)})

(defn- version-gen [overrides]
  #(s/gen ::version overrides))

(s/def ::major-version
  (s/with-gen
    major?
    (version-gen overrides)))

(s/def ::minor-version
  (s/with-gen
    minor?
    (version-gen (assoc overrides ::minor #(s/gen pos-int?)))))

(s/def ::patch-version
  (s/with-gen
    patch?
    (version-gen (assoc overrides ::patch #(s/gen pos-int?)))))

(s/def ::pre-release-version
  (s/with-gen
    pre-release?
    (version-gen (assoc overrides ::pre-release #(s/gen ::spec/non-blank-string)))))

(s/def ::build-version
  (s/with-gen
    build?
    (version-gen (assoc overrides ::build #(s/gen ::spec/non-blank-string)))))

(comment
  (s/exercise ::version-string)
  (s/exercise ::version)

  (s/exercise ::major)

  (s/exercise ::major-version)
  (s/exercise ::minor-version)
  (s/exercise ::patch-version)
  (s/exercise ::pre-release-version)
  (s/exercise ::build-version))
