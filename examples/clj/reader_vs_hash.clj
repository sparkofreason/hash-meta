(ns reader-vs-hash
  (:require [hash-meta.core :as ht :refer [defhashtag defreader]]
            [clojure.pprint :refer [pprint]]))

(defreader reader-p
  (fn [f]
    `(let [x# ~f]
       (pprint {:result x#
                :form '~f})
       x#)))

(defhashtag hashtag-p
  (fn [f f']
    `(let [x# ~f]
       (pprint {:result x#
                :form '~f'})
       x#)))


(inc #reader-p (* 2 #reader-p (+ 3 #reader-p (* 4 5))))
(inc #hashtag-p (* 2 #hashtag-p (+ 3 #hashtag-p (* 4 5))))
