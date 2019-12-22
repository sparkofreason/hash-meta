(ns hasht
  (:require [hash-f.core :refer :all]
            [clojure.pprint :refer [pprint]]))

(defhashfn t/foo #(tap> (assoc % :tag :foo)))
(defhashfn t/bar #(tap> (assoc % :tag :bar)))

(defn mean [xs]
  (/ (double #t/foo (reduce + xs)) #t/bar (count xs)))

(defn tap-fn
  [x]
  (case (:tag x)
    :foo (println "FOOOOOOOOOO!")
    :bar (println "BAAAAAAAAAR!"))
  (pprint (select-keys x [:form :result])))

(add-tap tap-fn)

(mean [1 4 5 2])

(Thread/sleep 1000)
(remove-tap tap-fn)
