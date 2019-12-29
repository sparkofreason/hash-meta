(ns hashpp
  (:require [hash-meta.core :as ht :refer [defhashtag]]
            [clojure.pprint :refer [pprint]]))

(def locals-sym (gensym "locals"))

(defmacro locals
  []
  ; Dubious method for determining if we're in CLJS, but it works for now.
  (->> (if (contains? &env :js-globals) (:locals &env) &env)
       (remove (fn [[name _]] (= name locals-sym)))
       (map (fn [[name _]] `[~(keyword name) ~name]))
       (into {})))

(defn pp-fn
  [f f' _]
  `(let [~locals-sym (locals)
         x# ~f]
     (pprint {:result x#
              :form '~f'
              :locals ~locals-sym})
     x#))

(defhashtag pp pp-fn)

(inc #pp (* 2 #pp (+ 3 #pp (* 4 5))))

(defn f
  [x]
  (let [a #pp (inc x)
        b #pp (* 2 a)]
    (dec #pp (- b #pp (* a #pp (Math/pow b 2))))))

(defn g
  [x]
  (* 3 #pp (f x)))

(g 5)

(defhashtag pp->>
  (fn [f f' _]
    `((fn [x#]
        (let [result# (->> x# ~f)]
          (pprint {:result result#
                   :form '~f'})
          result#)))))

(->> (range 10)
     #pp->> (filter odd?)
     (map inc))

(defhashtag p2
  (fn [f f' _]
    `(let [r# ~f]
       (println "FOO" '~f' r#)
       r#)))

(inc #p2 (* 2 #pp (+ 3 #p2 (* 4 5))))
