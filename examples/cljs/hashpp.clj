(ns hashpp
  (:require [hash-meta.core :as ht :refer [defhashtag]]
            [net.cgrand.macrovich :as macros]))

(defhashtag pp
  (fn [f f']
      `(let [r# ~f]
         (macros/case
          :clj (println '~f' "=>" r#)
          :cljs (.log js/console (str '~f' " => " r#)))
         r#)))
