(ns helmsman-test
  (:require [clojure.test :refer :all]
            [helmsman :refer :all]
            [ring.mock.request :refer [request]]))

(def default-output 
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body "Hello world."})

(def default-handler (constantly default-output))

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

(defn test-requests-against-handler
  [handler requests]
  (doseq [r requests]
    (is (= (handler r) default-output))))

(deftest compiling-routes-test
  (testing "Single definition"
    (test-requests-against-handler
      (compile-routes single-definition)
      valid-requests-single))
  (testing "Basic definition"
    (test-requests-against-handler
      (compile-routes basic-definition)
      valid-requests-basic))
  (testing "Single nested definition"
    (test-requests-against-handler
      (compile-routes single-nested-definition)
      valid-requests-single-nested))
  (testing "Many nested definition"
    (test-requests-against-handler
      (compile-routes many-nested-definition)
      valid-requests-many-nested)))

