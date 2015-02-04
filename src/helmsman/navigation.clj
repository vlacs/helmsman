(ns helmsman.navigation
  (:require
    [clojure.set]
    [helmsman.uri :as uri]
    [helmsman.navigation.preds :as preds]))

(defn meta-filter
  "Takes in a set of meta-data and a predicate and returns a sub-set
  for all items in the set where pred returns true."
  [meta-data pred?]
  (clojure.set/select pred? meta-data))

(defn meta-get-unique
  [meta-data pred?]
  (first (meta-filter meta-data pred?)))

(defn meta-with-id
  [meta-data id]
  (meta-get-unique meta-data (preds/with? :id id)))

(defn id->uri-path
  [request id]
  (let [current-uri-path (get-in request [:helmsman :uri-path])
        meta-data (get-in request [:helmsman :all-meta])]
    (if-let [meta-item (meta-with-id meta-data id)]
      (uri/relative-uri
        current-uri-path (:uri-path meta-item)))))

(defn id->uri
  "This creates a URI string from the current path from the passed request to the uri-
  path for the route with the passed unique meta-data id. Returns nil if there is no
  meta-data or item with the given id."
  [request id & args]
  (if-let [uri-path (id->uri-path request id)]
    (apply
      (partial
        uri/assemble
        (id->uri-path request id))
      args)))

