(ns elision
  (:require [sparkofreason.hash-meta.core :refer [defreader-n]]))

(def ^:dynamic *debug?* true)

(defreader-n tap
  (fn [f f' m]
    (if *debug?*
      `(let [r# ~f]
         (tap> {:form '~f'
                :result r#
                :metadata ~m})
         r#)
      `~f)))

(add-tap println)

(inc #tap (* 2 #tap (+ 3 #tap (* 4 5))))

(alter-var-root #'*debug?* (fn [_] false))

(inc #tap (* 2 #tap (+ 3 #tap (* 4 5))))

(remove-tap println)
