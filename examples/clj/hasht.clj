(ns hasht
  (:require [hash-meta.core :as ht :refer [defreader-n]]
            [clojure.pprint :refer [pprint]]))

(defreader-n t
  (fn [f _ _]
    `(time ~f)))

(defn f
  [n]
  (let [n (bigint n)]
    (loop [r 1N
           i 1N]
      (println r i)
      (if (> i n)
        r
        (recur (* r i) (inc i))))))

(defn g
  [n]
  (let [fact #t (f n)]
    (* fact fact)))

(g 1000)
