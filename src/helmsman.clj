(ns helmsman
  (:require [compojure.core]
            [compojure.handler]
            [taoensso.timbre :as timbre]
            [helmsman.tree :as tree]
            [helmsman.routes :as routes]))
(timbre/refer-timbre)

;;; Creates a handler from the description data structure.
(defn compile-routes
  [definition]
  (loop [state (tree/make-new-trio definition)]
    (let [new-state (tree/process-current state)
          next-item-state (tree/next-item new-state)]
      (if (nil? next-item-state)
        (apply
          routes/combine
          (tree/flatten-all-routes new-state))
        (recur next-item-state)))))

