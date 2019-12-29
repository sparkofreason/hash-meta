(ns with-meta
  (:require [hash-meta.core :as ht :refer [defreader-n]]))

(defreader-n t
  (fn foo [f f' m]
    `(let [r# ~f]
       (println '~f' "=>" r# "<" (:t ~m "") ">")
       r#)))

(inc #t ^{:t :foo} (* 2 #t (+ 3 #t ^{:t "BAR" :other :metadata} (* 4 5))))

(let [a 5]
  (* #t ^{:a :b} a 2))
