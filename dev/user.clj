(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [clojure.pprint :refer [pprint]]
            [clojure.repl :refer :all]
            [plumber.core :as plumber]
            ))

(def test-data
  [[:any "/" (constantly "A redirect!")]

   ^{:main-menu true
     :main-menu-weight 50
     :name "Home page"
     :desc "Aspire onboarding page."}
   [:get "/welcome" (constantly "Onboarding")]

   ^{:main-menu true
     :main-menu-weight 50
     :name "Administration"
     :desc "Where Aspire gets administered."}
   [:get "/admin" (constantly "Administration")
    [:get "/debug" #(prn-str %)]]

   [:context "/config"
    [:put "/key/:key" (constantly "Config key")]
    [:post "/page/:page" (constantly "Config page")]]

   [:any "/logout"  (constantly "Nothing here but us chickens.")]])

(def system nil)

(defn make-app
  []
  {:test-data test-data})

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

