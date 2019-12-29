(ns hash-meta.core
  (:require [clojure.walk :as walk]
            [clojure.core.unify :as u]))

(def transforms (atom #{}))

(defn- hide-hashtag-form
  [form]
  (loop [[transform & transforms] @transforms]
    (if transform
      (let [template (transform '?form '?form' '?meta)
            u (u/unify template form)]
        (if u
          (u '?form')
          (recur transforms)))
      form)))

(defn- make-transform
  [transform hide-nested?]
  (when hide-nested? (swap! transforms conj transform))
  (fn [form]
    (let [m (meta form)]
      (if hide-nested?
        (let [orig-form (walk/postwalk hide-hashtag-form form)]
          `~(transform form orig-form m))
        `~(transform form m)))))

(defn- make-reader
  [id transform hide-nested?]
  `(do
      (def ~id (#'make-transform ~transform ~hide-nested?))
      (set! *data-readers* (assoc *data-readers*
                                  '~id #'~id))
      #'~id))

(defmacro defreader-n
  "Defines and registers a \"nestable tagged literal\" reader macro which will transform
   the tagged form and hide the effects of nested hashtag expansions.
      * id - the name of the tag, e.g. p -> #p, foo/bar -> #foo/bar.
      * transform - a 2 argument function. This function is used at macro-expansion,
        so the usual macro rules apply. The first argument is the form
        as seen by the compiler, including effects of nested hashtag
        expansions. The second is the \"original\" form, as it would be seen
        before expansion (and without any hashtags). Another way to think about
        it is that the first form is the one you want to execute, while the
        second is for display."
  [id transform]
  (make-reader id transform true))

(defmacro defreader
  [id transform]
  "Defines and registers a \"tagged literal\" reader macro which will transform
   the tagged form.
      * id - the name of the tag, e.g. p -> #p, foo/bar -> #foo/bar.
      * transform - a 1 argument function. This function is used at macro-expansion,
        so the usual macro rules apply. The argument is the tagged form."
  (make-reader id transform false))
