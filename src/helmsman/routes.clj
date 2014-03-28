(ns helmsman.routes
  (:require [helmsman.uri :as uri]
            [compojure.core :as compojure]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(def http-methods
  #{:get :head :post
    :put :delete :trace
    :options :connect :patch})

(defn http-method?
  "Returns true if the provided argument is a http method."
  [method]
  (contains? http-methods method))

(defn any-http-method?
  "Returns true if the method is :any, does not match any http-method like
  (http-method?)"
  [method]
  (= method :any))
 
(defn route?
  "Returns true if the method is :any or a specific http method."
  [method]
  (or (http-method? method)
      (any-http-method? method)))
 
(defn context?
  "Checks the argument to see if it's a :context"
  [method]
  (= method :context))
 
(defn middleware?
  "Any method that is a fn is a middleware. Returns true if the agument is a fn."
  [method]
  (fn? method))
 
(defn cons-route
  [method uri route-fn]
  (compojure/make-route
    ;;; rewrite :any to nil for compojure.
    (if (any-http-method? method)
      nil method)
    uri
    route-fn))

(defn rewrite-uri
  [route uri-path]
  (assoc route 1 uri-path))
 
(defn combine
  [& routes-vec]
  (apply compojure/routes (vec routes-vec)))

