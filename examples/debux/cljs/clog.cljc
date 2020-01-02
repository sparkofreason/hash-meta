(ns cljs.clog
  #?(:clj (:require [sparkofreason.hash-meta.core :as ht :refer [defreader-n]])
     :cljs (:require [debux.cs.core :as d :refer-macros [clog]])))

#?(:clj (defreader-n clog
          (fn [form orig-form _]
            `(debux.cs.core/clog ~form))))
