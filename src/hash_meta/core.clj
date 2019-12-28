(ns hash-meta.core
  (:require [clojure.walk :as walk]
            [clojure.core.unify :as u]))

(defn hide-hashtag-form
  [transform form]
  (let [template (transform '?form '?form')
        u (u/unify template form)]
    (if u
      (u '?form')
      form)))

(defn make-hashtag
  [transform]
  (fn [form]
    (let [orig-form (walk/postwalk (partial hide-hashtag-form transform) form)]
      `~(transform form orig-form))))

(defmacro defhashtag
  "Defines and registers a \"tagged literal\" reader macro which calls hander-fn
   with data for debugging the tagged form.
      * id - the name of the tag, e.g. p -> #p, foo/bar -> #foo/bar.
      * handler-fn - a function of one argument with spec :hashtag.core/debug-data.
      * opts - option key/value pairs.
         ** :locals? (false) - Default false. includes local bindings as a map."
  [id transform]
  `(do
      (def ~id (make-hashtag ~transform))
      (set! *data-readers* (assoc *data-readers*
                                  '~id #'~id))
      #'~id))
