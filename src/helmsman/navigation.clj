(ns helmsman.navigation
  (:require
    [helmsman.tree :as tree]
    [helmsman.uri :as uri]))

(def navigation-meta-tags
  [:id :name :weight])

(defn pred-by-id
  [id]
  (fn [i]
    (if
      (contains? i :id)
      (let [m-id (get i :id)]
        (if (= m-id id)
          true
          false))
      false)))

(defn meta-from-request
  [request pred-fn]
  (let [meta-data (get-in request [:helmsman :all-meta])]
    (loop [md (vec meta-data)]
      (if (empty? md)
        nil
        (let [i (first md)]
          (if
            (pred-fn i)
            i
            (recur (vec (rest md)))))))))

(defn meta-id
  "Gets an item out of the meta data with a particular id stored in helmsman's litte
  corner of the ring request. Ids are expected to be unique."
  [request id]
  (meta-from-request request (pred-by-id id)))

(defn id->path
  [request id]
  (:uri-path (meta-id request id)))

;; TODO: Rename this fn to something less hyphenated. --jdoane
(defn id-to-uri
  "This creates a URI string from the current path from the passed request to the uri-
  path for the route with the passed unique meta-data id."
  [request id]
  (uri/relative-uri-str
    request
    (id->path request id)))
