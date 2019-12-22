(ns hashpp
  (:require [hash-f.core :refer :all]
            [clojure.pprint :refer [pprint]]))

(defhashfn pp pprint)
(defhashfn pp/locals pprint :locals true)
(defhashfn pp/fn pprint :stacktrace-tx current-frame)
(defhashfn pp/clojure pprint :stacktrace-tx clojure-frames)
(defhashfn pp/all pprint :stacktrace-tx all-frames)

(ppf '(dec b))
(defn f
  [x]
  (let [a (inc x)
        b (* 2 a)]
    #pp/all (dec b)))

(defn g
  [x]
  (* 3 (f x)))

(g 5)
