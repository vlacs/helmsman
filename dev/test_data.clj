(ns test-data
  (:require [plumber.core :refer :all]))

(def test-route-tree
  ;;; First we describe the route, similarly to how we would with GET and POST,
  ;;; but without binding or the GET/POST/etc. macros. If you want access to the
  ;;; request, you must pass a fn into the third argument.
  (desc-route
    :get "/foo" "Hello world. #1"
    ;;; Next we can perform a series of transforms on the route and
    ;;; it's mapped meta data. There are three pre-existant mechanisms
    ;;; for altering the described route.
    ;;;
    ;;; add-meta - Simply adds data to the meta map for only this
    ;;;   route.
    ;;; add-children - Takes in any number of (desc-route) results and
    ;;;   applies them to this route as children. This means that they
    ;;;   call within the same context as the URI as the parent route.
    ;;;   So if "/foo" has a child "/bar" the resultant uri would be
    ;;;   /foo/bar to reach the child route.
    ;;; apply-middleware - Applies a (or many) middlewares. In the
    ;;;   context of desc-route, the current route will be threaded
    ;;;   through all the items inside apply-middleware, much like how
    ;;;   threading (->) macro works.
    
    (add-meta {:name "Hello world #1 Page"})
    (add-children
      [(desc-route
         :get "/bar" "Hello world. #2 - It's a new world afterall!"
         (add-meta {:name "It's a new world afterall!"}))]))) 
