(ns plumber.core
  (:require [compojure.core :refer [make-route context routes]]
            [ring.middleware.params :refer  [wrap-params]] ))

(defn get-children-routes
  [children]
  (if children
    (into [] (map (fn [i] (:routes i)) children))
    []))

(defn strip-children-routes
  [children]
  (if (empty? children) nil
    (into [] (map #(dissoc % :routes) children))))

(defmacro apply-context
  [uri children]
  `(context ~uri [] ~@children))

(defmacro apply-middleware
  [desc & middlewares]
  `(assoc ~desc :routes
          (-> (:routes ~desc)
              ~@middlewares)))

(defn- apply-children-to-desc
  [desc children]
  (-> desc
      (assoc :routes
             (routes
               (apply-context (:uri desc)
                              (get-children-routes children))
               (:routes desc)))
      (assoc :children (into (get desc :children) (strip-children-routes children)))))

(defn add-children
  ([desc child-item & children]
   ;;; Child item could be a single child or a vector/list/seq of children.
   (apply-children-to-desc desc                          
                           (if (empty? children)
                             (if (vector? child-item)
                               child-item
                               [child-item])
                             (cons child-item children)))))

(defn add-meta
  [desc meta-map]
  (merge desc meta-map))

(defmacro desc-route
  ([route-method route-uri route-fn & transforms]
   `(-> {:routes (make-route ~route-method ~route-uri ~route-fn)
         :uri ~route-uri
         :children []}
        ~@transforms)))

(defn has-children?
  [desc]
  (if (empty? (:children desc))
    false true))

