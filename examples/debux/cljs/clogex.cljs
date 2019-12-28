(ns cljs.clogex
  (:require [cljs.clog :include-macros true]))

;;;; clog examples


; ### Basic usage

; This is a simple example. The macro *clog* prints an original form and pretty-prints
; the evaluated value on the REPL window. Then it returns the value without stopping
; code execution.

(* 2 #clog (+ 10 20))
; => 60

; Sometimes you need to see several forms evaluated. To do so, a literal vector
; form can be used like this.

(defn my-fun
  [a {:keys [b c d] :or {d 10 b 20 c 30}} [e f g & h]]
  #clog [a b c d e f g h])

(my-fun (take 5 (range)) {:c 50 :d 100} ["a" "b" "c" "d" "e"])
; => [(0 1 2 3 4) 20 50 100 "a" "b" "c" ("d" "e")]


; Notice that the printed value is a map, not a vector and the form
; is prepended with colon to differenciate the form from the evaluated value.

; Further examples:
(def a 10)
(def b 20)

#clog [a b [a b] :c]
; => [10 20 [10 20] :c]


; (-> {:a [1 2]}
;     (clog (get :a))
;     (conj 3))
; java.lang.IllegalArgumentException
; Don't know how to create ISeq from: java.lang.Long

#clog (-> "a b c d"
         .toUpperCase
         (.replace "A" "X")
         (.split " ")
         first)

(def five 5)
#clog (->> five (+ 3) (/ 2) (- 1))

#clog (let [a (take 5 (range))
            {:keys [b c d] :or {d 10 b 20 c 30}} {:c 50 :d 100}
            [e f g & h] ["a" "b" "c" "d" "e"]]
       [a b c d e f g h])
; => [(0 1 2 3 4) 20 50 100 "a" "b" "c" ("d" "e")]


(def c #clog (comp inc inc +))
(c 10 20)
