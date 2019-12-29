;;; Derived from https://github.com/weavejester/hashp/blob/master/src/hashp/core.clj

(ns hashp.core
  (:require [hash-meta.core :as ht :refer [defreader-n]]
            [clj-stacktrace.core :as stacktrace]
            [puget.printer :as puget]
            [puget.color.ansi :as color]))

(defn current-stacktrace []
  (->> (.getStackTrace (Thread/currentThread))
       (drop 3)
       (stacktrace/parse-trace-elems)))

(defn trace-str [trace]
  (when-let [t (first (filter :clojure trace))]
    (str "[" (:ns t) "/" (:fn t) ":" (:line t) "]")))

(def result-sym (gensym "result"))

(defn- hide-p-form [form]
  (if (and (seq? form)
           (vector? (second form))
           (= (-> form second first) result-sym))
    (-> form second second)
    form))

(def lock (Object.))

(def prefix (color/sgr "#p" :red))

(def print-opts
  (merge puget/*options*
         {:print-color    true
          :namespace-maps true}))

(defreader-n p
  (fn [form orig-form _]
    `(let [result# ~form]
         (locking lock
           (println
            (str prefix
                 (color/sgr (trace-str (current-stacktrace)) :green) " "
                 (when-not (= result# '~orig-form)
                   (str (puget/pprint-str '~orig-form print-opts) " => "))
                 (puget/pprint-str result# print-opts)))
           result#))))

(defn mean [xs]
  (/ (double #p (reduce + xs)) #p (count xs)))

(mean [1 4 5 2])

(defn f
  [x]
  (let [a #p (inc x)
        b #p (* 2 a)]
    (dec #p (- b #p (* a #p (Math/pow b 2))))))

(f 5)
