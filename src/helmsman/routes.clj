(ns helmsman.routes
  (:require [compojure.core :as compojure]
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

(defn assemble-uri
  "This fn puts together a uri from two parts. It also strips out all double slashes
  which is a result from constructing nested URIs and not enforcing empty URIs for
  slash-only uris."
  [base-uri-vector current-uri]
  ;;; TODO: Eliminate trailing slashes in the future.
  (clojure.string/replace 
    (str (apply str base-uri-vector) current-uri)
    #"//" "/"))

(defn realize-uri
  "Creates a full URI to use for oh so many things."
  [base-uri-vector current-route]
  (assemble-uri base-uri-vector (second current-route)))

(defn realize-route
  "Running this fn on the uri vector and current-route alters the route definition
  to make the URI reflect the full URI within the entire definition then returns the
  revised route definition which can be structed by compojure."
  [base-uri-vector current-route]
  (assoc current-route 1 (realize-uri base-uri-vector current-route)))

(defn assemble-route
  "Pulls information about the current route, incorporates the full URI by 'realizing'
  it and returns a compojure route."
  [route base-uri]
  (apply cons-route (realize-route base-uri route)))
 
(defn combine
  [& routes-vec]
  (apply compojure/routes routes-vec))
