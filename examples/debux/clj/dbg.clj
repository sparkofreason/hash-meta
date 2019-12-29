;;; Derived from https://github.com/philoskim/debux/blob/master/examples/src/clj/examples/dbg.clj

(ns clj.dbg
  (:require [hash-meta.core :as ht :refer [defhashtag]]
            [debux.core :as debux]))

(defhashtag dbg
  (fn [form orig-form _]
    `(debux/dbg ~form)))

;;;; dbg examples


; ### Basic usage

; This is a simple example. The macro *dbg* prints an original form and pretty-prints
; the evaluated value on the REPL window. Then it returns the value without stopping
; code execution.

(* 2 #dbg (+ 10 20))
; => 60


; Sometimes you need to see several forms evaluated. To do so, a literal vector
; form can be used like this.

(defn my-fun
  [a {:keys [b c d] :or {d 10 b 20 c 30}} [e f g & h]]
  #dbg [a b c d e f g h])

(my-fun (take 5 (range)) {:c 50 :d 100} ["a" "b" "c" "d" "e"])
; => [(0 1 2 3 4) 20 50 100 "a" "b" "c" ("d" "e")]


; Further examples:
(def a 10)
(def b 20)

#dbg [a b [a b] :c]
; => [10 20 [10 20] :c]


; (-> {:a [1 2]}
;     (dbg (get :a))
;     (conj 3))
; java.lang.IllegalArgumentException
; Don't know how to create ISeq from: java.lang.Long

#dbg (-> "a b c d"
         .toUpperCase
         (.replace "A" "X")
         (.split " ")
         first)

(def person
  {:name "Mark Volkmann"
   :address {:street "644 Glen Summit"
             :city "St. Charles"
             :state "Missouri"
             :zip 63304}
   :employer {:name "Object Computing, Inc."
              :address {:street "12140 Woodcrest Dr."
                        :city "Creve Coeur"
                        :state "Missouri"
                        :zip 63141}}})

#dbg (-> person :employer :address :city)
; => "Creve Coeur"

(def c 5)

#dbg (->> c (+ 3) (/ 2) (- 1))



#dbg (let [a (take 5 (range))
           {:keys [b c d] :or {d 10 b 20 c 30}} {:c 50 :d 100}
           [e f g & h] ["a" "b" "c" "d" "e"]]
       [a b c d e f g h])
; => [(0 1 2 3 4) 20 50 100 "a" "b" "c" ("d" "e")]


(def c #dbg (comp inc inc +))
(c 10 20)


#dbg (+ 10 (* 2 (- 100 3)))
