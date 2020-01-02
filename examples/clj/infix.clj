(ns infix.clj
  (:require [sparkofreason.hash-meta.core :as ht :refer [defreader]]))

(defreader i
  (fn [f _]
    (let [[v1 op v2] f]
      `(~op ~v1 ~v2))))

#i (2 + 3)
