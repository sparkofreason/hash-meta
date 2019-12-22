(ns hash-f.core
  (:require [clj-stacktrace.core :as stacktrace]
            [clojure.walk :as walk]))

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

(def default-opts {:locals false
                   :stacktrace-tx nil})

(defn make-hashfn
  [handler-fn-sym opts]
  (let [opts (merge default-opts opts)]
    (fn [form]
      (let [orig-form (walk/postwalk hide-p-form form)
            stacktrace-tx (:stacktrace-tx opts)
            locals? (:locals opts)]
        `(let [locals# (when ~locals? (locals))
               ~result-sym ~form
               debug-data# (cond-> {:form '~orig-form :result ~result-sym}
                                   ~locals? (assoc :locals locals#)
                                   (some? ~stacktrace-tx) (assoc :stacktrace (sequence ~stacktrace-tx (current-stacktrace))))]
           (~handler-fn-sym debug-data#)
           ~result-sym)))))

(defmacro defhashfn
  "Defines and registers a \"tagged literal\" reader macro which calls hander-fn
   with data for debugging the tagged form.
      * id - the name of the tag, e.g. p -> #p, foo/bar -> #foo/bar.
      * handler-fn - a function of one argument with spec ::hash-f.core/trace-data.
      * opts - option key/value pairs.
         ** :locals? (false) - Default false. includes local bindings as a map.
         ** :stacktrace-tx (nil) - a transducer to process stackframes as defined in clj-stacktrace"
  [id handler-fn & {:as opts}]
  (let [id' (-> id str (clojure.string/replace #"/" "-") symbol)]
    `(do
       (def ~id'
         (make-hashfn ~handler-fn '~opts))
       (set! *data-readers* (assoc *data-readers* '~id ~id'))
       #'~id')))
