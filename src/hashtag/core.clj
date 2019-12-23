(ns hashtag.core
  (:require [clj-stacktrace.core :as stacktrace]
            [clojure.walk :as walk]
            [clojure.spec.alpha :as s]))


(s/def ::file string?)
(s/def ::line pos-int?)
(s/def ::java boolean?)
(s/def ::clojure boolean?)
(s/def ::class string?)
(s/def ::method string?)
(s/def ::ns string?)
(s/def ::fn string?)
(s/def ::anon-fn boolean?)
(s/def ::java-frame (s/keys :opt-un [::file ::line ::java ::class ::method]))
(s/def ::clojure-frame (s/keys :opt-un [::file ::line ::clojure ::ns ::fn ::anon-fn]))
(s/def ::stack-frame (s/or :java ::java-frame :clojure ::clojure-frame))
(s/def ::stacktrace (s/coll-of ::stack-frame))

(s/def ::locals (s/map-of keyword? any?))

(s/def ::form any?)
(s/def ::result any?)

(s/def ::debug-data (s/keys :req-un [::form ::result] :opt-un [::locals ::stacktrace]))

(defn current-stacktrace []
  (->> (.getStackTrace (Thread/currentThread))
       (drop 3)
       (stacktrace/parse-trace-elems)))

(def result-sym (gensym "result"))

(defn- hide-p-form [form]
  (if (and (seq? form)
           (vector? (second form))
           (= (-> form second first) result-sym))
    (-> form second second)
    form))

(defmacro locals
  []
  (->> &env
       (map (fn [[name _]] `[~(keyword name) ~name]))
       (into {})))

(def all-frames (map identity))
(def clojure-frames (filter :clojure))
(def current-frame (comp clojure-frames (take 1)))

(def default-opts {:locals? false
                   :stacktrace-tx nil})

(defn make-hashtag
  [handler-fn-sym opts]
  (let [opts (merge default-opts opts)]
    (fn [form]
      (let [orig-form (walk/postwalk hide-p-form form)
            stacktrace-tx (:stacktrace-tx opts)
            locals? (:locals? opts)]
        `(let [locals# (when ~locals? (locals))
               ~result-sym ~form
               debug-data# (cond-> {:form '~orig-form :result ~result-sym}
                                   ~locals? (assoc :locals locals#)
                                   (some? ~stacktrace-tx) (assoc :stacktrace (sequence ~stacktrace-tx (current-stacktrace))))]
           (~handler-fn-sym debug-data#)
           ~result-sym)))))

(defmacro defhashtag
  "Defines and registers a \"tagged literal\" reader macro which calls hander-fn
   with data for debugging the tagged form.
      * id - the name of the tag, e.g. p -> #p, foo/bar -> #foo/bar.
      * handler-fn - a function of one argument with spec :hashtag.core/debug-data.
      * opts - option key/value pairs.
         ** :locals? (false) - Default false. includes local bindings as a map.
         ** :stacktrace-tx (nil) - a transducer to process stackframes as defined in clj-stacktrace"
  [id handler-fn & {:as opts}]
  (let [id' (-> id str (clojure.string/replace #"/" "-") symbol)]
    `(do
       (def ~id'
         (make-hashtag ~handler-fn '~opts))
       (set! *data-readers* (assoc *data-readers* '~id ~id'))
       #'~id')))
