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
  (or
    (set? method)
    (http-method? method)
    (any-http-method? method)))

(defn context?
  "Checks the argument to see if it's a :context"
  [method]
  (or
    (vector? method)
    (= method :context)
    (string? method)))
 
(defn middleware?
  "Any method that is a fn is a middleware. Returns true if the agument is a fn."
  [method]
  (or
    (and
      (var? method)
      (fn? (var-get method)))
    (fn? method)))

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

(defn stanza-min-length
  [item]
  (let [[hpi spi & remaining] item]
    (cond
      (middleware? hpi) (count item)
      (context? hpi) (cond 
                        (keyword? hpi) (if (string? spi) 2 1)
                        (string? hpi) 1
                        (vector? hpi) 0)
      (route? hpi) (cond
                     (string? spi) 3
                     (fn? (if (var? spi) (var-get spi) spi)) 2))))

(defn extract-path
  [[hpi spi & _]]
  (cond 
    (string? hpi) hpi
    (and (not (middleware? hpi)) (string? spi)) spi
    :default ""))
 
