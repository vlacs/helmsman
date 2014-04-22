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
   (nav/meta-with-id
     (get-in request [:helmsman :all-meta])
     :static-assets)})

(def navigation-test-structure
  [^{:name "Home page"
     :id :home
     :main-menu true}
   [:get "/" basic-handler]
   ^{:name "The Foobar Page!"
     :id :foobar
     :main-menu true}
   [:get "/foobar" basic-handler]
   ^{:name "The Item Page!"
     :id :item-page}
   [:get "/item" basic-handler
    ^{:name "Nested Item Page!"
      :id :nested-item
      :main-menu true}
    [:get "/nested" basic-handler
     ^{:id :static-assets}
     [:get "/assets" basic-handler]]]])

;;; Navigation doesn't need routes, just meta-data.
(def app-meta (helmsman/compile-meta navigation-test-structure))

(deftest navigation-meta-search
  (testing "Browse meta by unique id."
    (is (not (nil? (nav/meta-with-id app-meta :static-assets))))))


