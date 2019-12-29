(ns with-meta
  (:require [hash-meta.core :as ht :refer [defhashtag]]))

(defhashtag t
  (fn foo [f f' m]
    `(let [r# ~f]
       (println '~f' "=>" r# "<" (:t ~m "") ">")
       r#)))

(t ^{:t "BAR" :foo :bar} (* 4 5))
(inc #t ^{:t :foo} (* 2 #t (+ 3 #t ^{:t "BAR" :foo :bar} (* 4 5))))

(let [a 5]
  (* #t ^{:a :b} a 2))
