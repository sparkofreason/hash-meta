(ns test
  #?(:clj (:require [hashpp])
     :cljs (:require-macros [hashpp])))

(inc #pp (* 2 #pp (+ 3 #pp (* 4 5))))
