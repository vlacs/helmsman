(ns helmsman-navigation-test
  (:require
    [clojure.test :refer :all]
    [helmsman :refer :all]
    [helmsman.navigation :as nav]
    [ring.mock.request :refer [request]]))

(def basic-page
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body "Hello world."})

(def basic-handler (constantly basic-page))

(defn basic-handler
  [request]
  {:rval
   (nav/meta-from-request
     request
     (nav/pred-by-id :static-assets))})

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
    [:get "/nested" basic-handler
     ^{:id :static-assets}
     [:get "/assets" basic-handler]]]])

(def app-handler (helmsman/compile-routes navigation-test-structure))

(deftest navigation-meta-search
  (testing "Browse meta by id."
    (is (= :static-assets
      (let [result (app-handler (request :get "/"))]
        (get-in result [:rval :id]))))))
