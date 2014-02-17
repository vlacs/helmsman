(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [clojure.pprint :refer [pprint]]
            [clojure.repl :refer :all]
            [test-data]
            [plumber.core :as plumber]))

(def system nil)

(defn make-app
  []
  {:test-routes test-data/test-route-tree})

(defn init
  "Sets up a basic state for us to work with."
  []
  (alter-var-root #'system
                  (constantly (make-app))))

(defn start
  "Nothing to really start up yet."
  []
  system)

(defn stop
  "Nothing to really stop yet."
  []
  (alter-var-root #'system (constantly nil)))

(defn go
  []
  (init)
  (start))

(defn reset
  []
  (stop)
  (refresh-all :after 'user/go))

