(ns helmsman-ring-test
  (:require [clojure.test :refer :all]
            [helmsman :refer :all]
            [ring.mock.request :refer [request]]))

(def middleware-req-item :test-item)
(def middleware-req-val "Apple Pie.")
(def middleware-req-val-alt "Blueberry pie.")

(defn default-middleware
  "Adds an item to the request."
  [handler item-name value]
  (fn [request]
    (handler (assoc request item-name value))))

(def default-output 
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body "Hello world."})

(def middleware-output-part
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}})

(def middleware-output-expectation
  (assoc middleware-output-part :body middleware-req-val))

(def middleware-output-expectation-alt
  (assoc middleware-output-part :body middleware-req-val-alt))

(def default-handler (constantly default-output))

(defn middleware-handler
  [request]
  (assoc middleware-output-part
         :body (middleware-req-item
                 request)))

(def single-definition
  [[:any "/" default-handler]])

(def valid-requests-single
  [(request :get "/")])

(def basic-definition
  [[:any "/" default-handler]
   [:any "/foo" default-handler]
   [:any "/bar" default-handler]])

(def valid-requests-basic
  [(request :get "/")
   (request :get "/foo")
   (request :get "/bar")])

(def single-nested-definition
  [[:any "/" default-handler
    [:any "/foo" default-handler
     [:any "/bar" default-handler]]]])

(def valid-requests-single-nested
  [(request :get "/")
   (request :get "/foo")
   (request :get "/foo/bar")])

(def many-nested-definition
  [[:any "/" default-handler
    [:any "/foo" default-handler
     [:any "/bar" default-handler
      [:any "/test" default-handler]]
     [:any "/baz" default-handler
      [:any "/test2" default-handler]]]]])

(def valid-requests-many-nested
  [(request :get "/")
   (request :get "/foo")
   (request :get "/foo/bar")
   (request :get "/foo/bar/test")
   (request :get "/foo/baz")
   (request :get "/foo/baz/test2")])

(def alt-many-nested-definition
  [[:any "/" default-handler
    [:any "/foo" default-handler
     [:any "/bar" default-handler
      [:any "/testing" default-handler]]]]
   [:any "/api" default-handler]])

(def valid-requests-alt-many-nested
  [(request :get "/")
   (request :get "/foo")
   (request :get "/foo/bar")
   (request :get "/foo/bar/testing")
   (request :get "/api")])

(def single-middleware-definition
  [[:any "/" middleware-handler]
   [default-middleware
    middleware-req-item
    middleware-req-val]])

(def single-middleware-valid-requests
  [(request :get "/")])

(def basic-middleware-definition
  [[:any "/" middleware-handler]
   [:any "/foo" middleware-handler]
   [:any "/bar" middleware-handler]
   [default-middleware
    middleware-req-item
    middleware-req-val]])

(def basic-middleware-valid-requests
  [(request :get "/")
   (request :get "/foo")
   (request :get "/bar")])

(def nested-middleware-definition
  [[:get "/" middleware-handler
   [:get "/foo" middleware-handler
    [:get "/bar" middleware-handler
     [:get "/testing" middleware-handler]]]]
   [default-middleware
    middleware-req-item
    middleware-req-val]])

(def nested-middleware-valid-responses
  [(request :get "/")
   (request :get "/foo")
   (request :get "/foo/bar")
   (request :get "/foo/bar/testing")])

(def complex-middleware-definition
  [[:get "/" middleware-handler
    [default-middleware
     middleware-req-item
     middleware-req-val]]
   [:get "/echo" middleware-handler
    [:get "/echo" middleware-handler
     [default-middleware
      middleware-req-item
      middleware-req-val]]]
   [:get "/foo" middleware-handler]
   [default-middleware
    middleware-req-item
    middleware-req-val-alt]])

(def complex-middleware-requests
  [(request :get "/")
   (request :get "/echo/echo")])

(def complex-middleware-requests-alt
  [(request :get "/echo")
   (request :get "/foo")])

(defn test-requests-against-handler
  [handler requests expected-output]
  (doseq [r requests]
    (is (= (handler r) expected-output))))

(deftest compiling-routes-test
  (testing "Testing single route definition"
    (test-requests-against-handler
      (compile-routes single-definition)
      valid-requests-single
      default-output))
  (testing "Testing basic single level definition"
    (test-requests-against-handler
      (compile-routes basic-definition)
      valid-requests-basic
      default-output))
  (testing "Tested one set of nested routes."
    (test-requests-against-handler
      (compile-routes single-nested-definition)
      valid-requests-single-nested
      default-output))
  (testing "Test more than one nested set of routes."
    (test-requests-against-handler
      (compile-routes many-nested-definition)
      valid-requests-many-nested
      default-output))
  (testing "Testing alternate nested sets of routes."
    (test-requests-against-handler
      (compile-routes alt-many-nested-definition)
      valid-requests-alt-many-nested
      default-output)))

(deftest compile-middleware-test
  (testing "Testing single definition with middleware."
    (test-requests-against-handler
      (compile-routes single-middleware-definition)
      single-middleware-valid-requests
      middleware-output-expectation))
  (testing "Testing basic definition with middleware."
    (test-requests-against-handler
      (compile-routes basic-middleware-definition)
      basic-middleware-valid-requests
      middleware-output-expectation))
  (testing "Testing nested definition with middleware."
    (test-requests-against-handler
      (compile-routes nested-middleware-definition)
      nested-middleware-valid-responses
      middleware-output-expectation))
  (testing "Testing 'complex' routes with mixed middleware."
    (let [compiled-routes (compile-routes complex-middleware-definition)]      
      (test-requests-against-handler
        compiled-routes
        complex-middleware-requests
        middleware-output-expectation)
      (test-requests-against-handler
        compiled-routes
        complex-middleware-requests-alt
        middleware-output-expectation-alt))))

