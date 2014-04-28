(ns user
  (:require [clojure.java.io :as io]
            [clojure.java.javadoc :refer (javadoc)]
            [clojure.pprint :refer (pprint)]
            [clojure.reflect :refer (reflect)]
            [clojure.repl :refer (apropos dir doc find-doc pst source)]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [ring.adapter.jetty :refer (run-jetty)]
            [helmsman :as h]
            [helmsman.example-site]
            ))

(def system nil)

(defn make-app
  []
  {:initialized true
   :site-handler (h/compile-routes helmsman.example-site/our-routes)
   :site-meta (h/compile-meta helmsman.example-site/our-routes)
   :web-server {:status :not-started}})

(defn init
  "Sets up a basic state for us to work with."
  []
  (alter-var-root #'system (constantly (make-app)))
  :initialized)

(defn start
  "Nothing to really start up yet."
  []
  (alter-var-root #'system
                  assoc-in
                  [:web-server :instance]
                  (run-jetty (:site-handler system)
                             {:port 8088 :join? false}))
  (alter-var-root #'system assoc-in [:web-server :status] :started)
  :started)

(defn stop
  "Nothing to really stop yet."
  []
  (when-let [instance (get-in system [:web-server :instance])]
    (.stop instance))
  (alter-var-root #'system assoc :web-server (dissoc (:web-server system) :instance))
  (alter-var-root #'system assoc-in [:web-server :status] :stopped)
  :stopped)

(defn go
  []
  (init)
  (start)
  :ready)

(defn reset
  []
  (stop)
  (refresh-all :after 'user/go))

