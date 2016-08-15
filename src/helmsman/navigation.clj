(ns helmsman.navigation
  (:require
    [helmsman.request :as req]
    [helmsman.uri :as uri]))

(defn get-route-by-id
  "Indexes are sets. In reality, no more than one route should have the same ID
  but if that ever is the case, we might as well keep it simple and take the
  first one in the set."
  [request route-id]
  (first (get (req/get-index-routes request :id) route-id)))

(defn get-relative-path
  "Based of the current path inside any given request, create a relative uri
  to another route based on a meta-data id."
  [request destination-id]
  (uri/relative-uri
    (get-in request [:helmsman :request-path])
    (:path (get-route-by-id request destination-id))))
 
(defn assemble-relative-uri
  [request destination-id & assembly-args]
  (apply
    (partial
      uri/assemble
      (get-relative-path request destination-id))
    assembly-args))
