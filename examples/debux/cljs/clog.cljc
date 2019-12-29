(ns cljs.clog
  #?(:clj (:require [hash-meta.core :as ht :refer [defhashtag]])
     :cljs (:require [debux.cs.core :as d :refer-macros [clog]])))

#?(:clj (defhashtag clog
          (fn [form orig-form _]
            `(debux.cs.core/clog ~form))))
