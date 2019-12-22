(ns hashtag.core-test
  (:require [hashtag.core :as ht :refer [defhashtag]]
            [cognitect.transcriptor :as xr :refer (check!)]))

(defn my-tap
  [x]
  (tap> (update x :stacktrace first)))

(defhashtag h my-tap :locals? true :stacktrace-tx ht/current-frame)

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
                {:file "core_test.clj",
                 :line 13,
                 :clojure true,
                 :ns "hashtag.core-test",
                 :fn "foo",
                 :anon-fn false}}
               {:result 5,
                :form '(foo x),
                :locals {:x 4},
                :stacktrace
                {:file "core_test.clj",
                 :line 17,
                 :clojure true,
                 :ns "hashtag.core-test",
                 :fn "baz",
                 :anon-fn false}}])
        @ts)
