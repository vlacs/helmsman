(ns helmsman-example-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer [request]]
            [helmsman :as h]
            [helmsman.navigation :as nav]
            [helmsman.uri :as uri]
            [helmsman.example-site :as es]))

(def application (h/compile-routes es/our-routes))
(def meta-data (h/compile-meta es/our-routes))

(def test-arg-1 123)
(def test-arg-2 456)

(def meta-id-map (dissoc (clojure.set/index meta-data [:id]) {}))

(def testable-requests
  (map (fn [i] (request :get
                        (uri/assemble (get (first (second i)) :uri-path) :one test-arg-1 :two test-arg-2))) meta-id-map))

;;; Consider writing tests for the example, this will have a lot of overlap with
;;; other tests that already test things that the example needs to run.
;;; TODO: Write tests for this.

