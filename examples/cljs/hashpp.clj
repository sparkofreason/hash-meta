(ns hashpp
  (:require [hash-meta.core :as ht :refer [defreader-n]]
            [net.cgrand.macrovich :as macros]))

(defreader-n pp
  (fn [f f' _]
      `(let [r# ~f]
         (macros/case
          :clj (println '~f' "=>" r#)
          :cljs (.log js/console (str '~f' " => " r#)))
         r#)))
