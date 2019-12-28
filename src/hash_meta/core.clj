(ns hash-meta.core
  (:require [clojure.walk :as walk]
            [clojure.core.unify :as u]))

(def transforms (atom #{}))

(defn hide-hashtag-form
  [form]
  (loop [[transform & transforms] @transforms]
    (if transform
      (let [template (transform '?form '?form')
            u (u/unify template form)]
        (if u
          (u '?form')
          (recur transforms)))
      form)))

(defn make-hashtag
  [transform]
  (swap! transforms conj transform)
  (fn [form]
    (let [orig-form (walk/postwalk hide-hashtag-form form)]
      `~(transform form orig-form))))

(defmacro defhashtag
  "Defines and registers a \"tagged literal\" reader macro which calls hander-fn
   with data for debugging the tagged form.
      * id - the name of the tag, e.g. p -> #p, foo/bar -> #foo/bar.
      * transform - a 2 argument function..."
  [id transform]
  `(do
     (def ~id (make-hashtag ~transform))
     (set! *data-readers* (assoc *data-readers*
                                 '~id #'~id))
     #'~id))
