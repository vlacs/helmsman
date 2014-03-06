(ns plumber.core
  (:require [compojure.core :as compojure]
            [taoensso.timbre :as timbre]
            [plumber.tree :as tree]))
(timbre/refer-timbre)

;;; Creates a handler from the description data structure.
(defn compile-routes
  [definition]
  (loop [state (tree/make-new-trio definition)]
    (let [new-state (tree/process-current state)
          next-item-state (tree/next-item new-state)]
      (if (nil? next-item-state)
        (compojure/routes (tree/flatten-routes new-state))
        (recur next-item-state)))))
