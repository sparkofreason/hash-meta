(ns hash-f.core-test
  (:require [hash-f.core :refer :all]
            [cognitect.transcriptor :as xr :refer (check!)]))

(defhashfn h tap> :locals true :stacktrace :fn)

(defn foo
  [x]
  (let [y (* 2 x)
        z #h (+ 1 x)]
    z))

(defn baz
  [x]
  #h (foo x))

(def ts (atom []))
(defn tap-fn
  [t]
  (swap! ts conj t))
(add-tap tap-fn)
(baz 4)
(Thread/sleep 1000)
(remove-tap tap-fn)

(check! #(= % [{:result 5,
                :form '(+ 1 x),
                :locals {:x 4, :y 8},
                :stacktrace
                [{:file "core_test.clj",
                  :line 32,
                  :clojure true,
                  :ns "hash-f.core-test",
                  :fn "foo",
                  :anon-fn false}]}
               {:result 5,
                :form '(foo x),
                :locals {:x 4},
                :stacktrace
                [{:file "core_test.clj",
                  :line 32,
                  :clojure true,
                  :ns "hash-f.core-test",
                  :fn "baz",
                  :anon-fn false}]}])
        @ts)
