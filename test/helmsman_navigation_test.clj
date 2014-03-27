(ns helmsman-navigation-test
  (:require
    [helmsman :refer :all]
    [helmsman.navigation :as nav]))

(def basic-page
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body "Hello world."})

(def basic-handler (constantly basic-page))

(def navigation-test-structure
  [^{:name "Home page"
     :id :home}
   [:get "/" basic-handler]
   ^{:name "The Foobar Page!"
     :id :foobar}
   [:get "/foobar" basic-handler]
   ^{:name "The Item Page!"
     :id :item-page}
   [:get "/item" basic-handler
    ^{:name "Nested Item Page!"
      :id :nested-item}
    [:get "/nested" basic-handler]]])
