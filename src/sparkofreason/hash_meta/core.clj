(ns sparkofreason.hash-meta.core
  (:require [clojure.walk :as walk]
            [clojure.core.unify :as u]))

(def transforms (atom {}))

(defn- hide-hashtag-form
  [form]
  (loop [[transform & transforms] (vals @transforms)]
    (if transform
      (let [template (transform '?form '?form' '?meta)
            u (u/unify template form)]
        (if u
          (u '?form')
          (recur transforms)))
      form)))

(defn- make-transform
  [id transform hide-nested?]
  (when hide-nested? (swap! transforms assoc id transform))
  (fn [form]
    (let [m (meta form)]
      (if hide-nested?
        (let [orig-form (walk/postwalk hide-hashtag-form form)]
          `~(transform form orig-form m))
        `~(transform form m)))))

(defn make-reader
  [id transform hide-nested?]
  `(do
      (def ~id (#'make-transform '~id ~transform ~hide-nested?))
      (set! *data-readers* (assoc *data-readers* '~id #'~id))
      #'~id))

(defmacro defreader-n
  "Defines a \"nestable tagged literal\" reader macro which will transform
   the tagged form and hide the effects of nested hashtag expansions.
      * id - the name of the tag, e.g. p -> #p.
      * transform - a 3 argument function. This function is used at macro-expansion,
        so the usual macro rules apply. The first argument is the form
        as seen by the compiler, including effects of nested hashtag
        expansions. The second is the \"original\" form, as it would be seen
        before expansion (and without any hashtags). Another way to think about
        it is that the first form is the one you want to execute, while the
        second is for display. The third argument is any metadata associated
        with the tagged form."
  [id transform]
  (make-reader id transform true))

(defmacro defreader
  "Defines a \"tagged literal\" reader macro which will transform
   the tagged form.
      * id - the name of the tag, e.g. p -> #p.
      * transform - a 2 argument function. This function is used at macro-expansion,
        so the usual macro rules apply. The first argument is the tagged form.
        The second argument is any metadata associated with the tagged form."
  [id transform]
  (make-reader id transform false))
