(ns helmsman.routes
  (:require [helmsman.uri :as uri]
            [compojure.core :as compojure]
            [compojure.route]
            [clojure.set]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(def http-methods
  #{:get :head :post
    :put :delete :trace
    :options :connect :patch})

(def path-bearing-keywords
  (clojure.set/union
    http-methods
    #{:files :resources :context :any}))

(def nestable-keywords
  (clojure.set/union
    http-methods
    #{:context :any}))

(def keyword-route-length
  (into
    {}
    (vec
      (conj
        (map #(vec [% 3]) (clojure.set/union http-methods #{:any}))
        [:context 2]))))

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

(defn resources?
  "Checks to see if the argument is :static."
  [method]
  (= method :resources))

(defn files?
  "Checks to see if the argument is :files."
  [method]
  (= method :files))

(defn not-found?
  [method]
  (= method :not-found))

(defn cons-static
  [static-fn path arg-seq]
  (apply (partial static-fn path) arg-seq))

(def cons-resources
  (partial cons-static compojure.route/resources))

(def cons-files
  (partial cons-static compojure.route/files))

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

