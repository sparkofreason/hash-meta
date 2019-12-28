(ns hash-meta.core-test
  (:require [hash-meta.core :as ht :refer [defhashtag]]
            [cognitect.transcriptor :as xr :refer (check!)]))

(defhashtag t
  (fn [f f']
    `(let [r# ~f]
       (tap> {:form '~f'
              :result r#})
       r#)))

(defhashtag t2
  (fn [f f']
    `(let [r# ~f]
       (tap> {:f '~f'
              :r r#})
       r#)))

(def ts (atom []))
(defn tap-fn
  [t]
  (swap! ts conj t))
(add-tap tap-fn)

;;; Check that nested forms are hidden
(inc #t (* 2 #t (+ 3 #t (* 4 5))))
(check! #(= [{:result 20, :form '(* 4 5)}
             {:result 23, :form '(+ 3 (* 4 5))}
             {:result 46, :form '(* 2 (+ 3 (* 4 5)))}]
            %)
        @ts)

(reset! ts [])

;;; Check that nested forms are hidden
(inc #t (* 2 #t2 (+ 3 #t (* 4 5))))
(check! #(= [{:result 20, :form '(* 4 5)}
             {:r 23, :f '(+ 3 (* 4 5))}
             {:result 46, :form '(* 2 (+ 3 (* 4 5)))}]
            %)
        @ts)

(remove-tap tap-fn)
