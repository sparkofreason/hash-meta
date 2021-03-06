(ns sparkofreason.hash-meta.core-test
  (:require [sparkofreason.hash-meta.core :as ht :refer [defreader-n]]
            [cognitect.transcriptor :as xr :refer (check!)]))

(defreader-n t
  (fn [f f' _]
    `(let [r# ~f]
       (tap> {:form '~f'
              :result r#})
       r#)))

(defreader-n t2
  (fn [f f' _]
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

(reset! ts [])

(defreader-n tm
  (fn [f f' m]
    `(let [r# ~f]
       (tap> {:form '~f'
              :result r#
              :meta (:a ~m)})
       r#)))


(inc #tm ^{:a 2} (* 2 #tm (+ 3 #tm ^{:a 4} (* 4 5))))

(check! #(= [{:meta 4, :result 20, :form '(* 4 5)}
             {:meta nil, :result 23, :form '(+ 3 (* 4 5))}
             {:meta 2, :result 46, :form '(* 2 (+ 3 (* 4 5)))}]
            %)
        @ts)

(remove-tap tap-fn)
