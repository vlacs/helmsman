(ns plumber.core
  (:require [compojure.core :refer [make-route context routes]]
            [ring.middleware.params :refer  [wrap-params]] ))

(defn get-children-routes
  [children]
  (if children
    (into [] (map (fn [i] (:routes i)) children))
    []))

(defn strip-children-routes
  [& children]
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

(defn apply-children
  [desc & children]
  (if (empty? children)
    desc
    (-> desc
        (assoc :routes 
               (routes
                 (apply-context (:uri desc) 
                                (apply get-children-routes children))
                 (:routes desc)))
        (assoc :children (into (get desc :children) (apply strip-children-routes children))))))

(defn apply-meta
  [desc meta-map]
  (merge desc meta-map))

(defmacro desc-route
  ([route-method route-uri route-fn & transforms]
   `(->
      (let [route# (make-route ~route-method ~route-uri ~route-fn)]
        {:routes route# :uri ~route-uri :children []})
      ~@transforms)))

(defn has-children?
  [desc]
  (if (empty? (:children desc))
    false true))

(defn get-first-child
  [desc]
  (first (:children desc)))

(defn drop-first-child
  "No, no, this isn't as bad as it sounds!
  We want to remove the child we're about to step into so we don't step into
  it again. This is a 'working' structure and by the time we are done should
  be completely empty."
  [desc]
  (assoc desc :children (rest (:children desc))))

(defn make-some-routes
  []
  (desc-route :get "/foo" "Hello World."
              (apply-meta {:hello :world})
              (apply-children (desc-route :get "/bar" "Another world."
                                          (apply-meta {:some :more-meta-data}))
                              (desc-route :get "/pineapple" "Yummie fruit!"
                                          (apply-meta {:fruit :citrus :name "Tasty pineapple"})))
              (apply-middleware (wrap-params))))
