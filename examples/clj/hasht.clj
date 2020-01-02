(ns hasht
  (:require [hash-meta.core :as ht :refer [defreader-n]]
            [clojure.pprint :refer [pprint]]))

;;; Simple
(defreader-n t
  (fn [f _ _]
    `(time ~f)))

#t (Thread/sleep 1000)

;;; More juicy
(defreader-n t+
  (fn [f f' _]
    `(let [start# (. System (nanoTime))
           r# ~f
           elapsed# (/ (double (- (. System (nanoTime)) start#)) 1000000.0)]
       (pprint {:result r#
                :form '~f'
                :elapsed-ms elapsed#})
       r#)))


(defn f
  [n]
  (let [n (bigint n)]
    (loop [r 1N
           i 1N]
      (if (> i n)
        r
        (recur (* r i) (inc i))))))

(defn g
  [n]
  (let [fact #t+ (f n)]
    (* fact fact)))

(g 100)
