(ns helmsman
  (:require [compojure.core]
            [compojure.handler]
            [taoensso.timbre :as timbre]
            [helmsman.tree :as tree]
            [helmsman.routes :as routes]
            [helmsman.middleware :as middleware]))
(timbre/refer-timbre)

(defn compile-meta
  [definition]
  (tree/gather-all-meta definition))

;;; Creates a handler from the description data structure.
(defn compile-routes
  [definition]
  (loop [state (tree/make-new-trio definition)]
    (let [new-state (tree/process-current state)
          next-item-state (tree/next-item new-state)]
      (if (nil? next-item-state)
        (middleware/attach-helmsman
          (apply
            routes/combine
            (tree/flatten-all-routes new-state))
          (tree/gather-all-meta definition))
        (recur next-item-state)))))

(defmacro handler
  "Create a handler that uses compojure destructuring syntax."
  [bindings & body]
  `(fn [request#]
     (compojure.core/let-request
       [~bindings request#]
       ~@body)))

(defmacro defhandler
  "Define a handler that uses compojure destructuring syntax."
  [defname bindings & body]
  `(def ~defname (handler ~bindings ~@body)))

