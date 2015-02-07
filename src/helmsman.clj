(ns helmsman
  (:require [compojure.core]
            [taoensso.timbre :as timbre]
            [helmsman.router :as router]))
(timbre/refer-timbre)

(def create-ring-handler router/create-ring-handler)

(defmacro handler
  " *** DEPRECATED, removing Compojure dep soon.  ***
  Create a handler that uses compojure destructuring syntax."
  [bindings & body]
  `(fn [request#]
     (compojure.core/let-request
       [~bindings request#]
       ~@body)))

(defmacro defhandler
  "Define a handler that uses compojure destructuring syntax."
  [defname bindings & body]
  `(def ~defname (handler ~bindings ~@body)))

