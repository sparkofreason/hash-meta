(ns infix.clj
  (:require [hash-meta.core :as ht :refer [defreader]]))

(defreader i 
  (fn [f]
    (let [[v1 op v2] f]
      `(~op ~v1 ~v2))))

#i (2 + 3)
