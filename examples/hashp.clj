(ns hashp
  (:require [hash-f.core :refer :all]
            [puget.printer :as puget]
            [puget.color.ansi :as color]))

(defn trace-str [t]
  (str "[" (:ns t) "/" (:fn t) ":" (:line t) "]"))

(def lock (Object.))

(def prefix (color/sgr "#p" :red))

(def print-opts
  (merge puget/*options*
         {:print-color    true
          :namespace-maps true}))

(defn puget-print
  [t]
  (locking lock
    (println
      (str prefix
           (color/sgr (trace-str (-> t :stacktrace first)) :green) " "
           (when-not (= (:result t) (:form t))
             (str (puget/pprint-str (:form t) print-opts) " => "))
           (puget/pprint-str (:result t) print-opts)))))

(defhashfn p puget-print :stacktrace :fn)

(defn mean [xs]
  (/ (double #p (reduce + xs)) #p (count xs)))

(mean [1 4 5 2])