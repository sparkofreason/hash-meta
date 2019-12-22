(ns hasht
  (:require [hashtag.core :as ht :refer [defhashtag]]
            [clojure.pprint :refer [pprint]]))

(defhashtag t/foo #(tap> (assoc % :tag :foo)))
(defhashtag t/bar #(tap> (assoc % :tag :bar)))

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
