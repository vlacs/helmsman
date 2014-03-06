(ns helmsman.tree
  (:require [clojure.zip :as zip]
            [taoensso.timbre :as timbre]
            [compojure.core :as compojure]))
(timbre/refer-timbre)

(def http-methods
  #{:get :head :post
    :put :delete :trace
    :options :connect :patch})

;;; What kind of object do we have?
(defn http-method?
  [method]
  (contains? http-methods method))

(defn any-http-method?
  [method]
  (= method :any))
 
(defn route?
  [method]
  (or (http-method? method)
      (any-http-method? method)))

(defn on-route?
  [zipper]
  (route? (zip/node zipper)))

(defn context?
  [method]
  (= method :context))

(defn on-context?
  [zipper]
  (context? (zip/node zipper)))

(defn middleware?
  [method]
  (fn? method))

(defn on-middleware?
  [zipper]
  (middleware? (zip/node zipper)))

(defn flatten-routes
  [trio-map]
  (flatten (zip/root (:routes trio-map))))

(defn first-item
  "Moves the zipper to the very first item item (route) that is encountered.
  The zipper is automatically moved to the root to start."
  [loc]
  (loop [zipper loc]
    (if (not (vector? (zip/node zipper)))
      zipper
      (recur (zip/next zipper)))))

(defn next-many
  "Moves the zipper to the next node n times then returns the zipper."
  [loc node-count]
  (loop [c 0 z loc]
    (if (>= c node-count)
      loc
      (recur (inc c) (zip/next z)))))

(defn next-nodes
  "Returns the next so many nodes in the tree."
  [loc node-count]
  (loop [c 0
         z loc
         nodes []]
    (if (>= c node-count)
      nodes
      (recur (inc c) (zip/next z) (conj nodes (zip/node z))))))

(defn processing-complete?
  [trio-map]
  (zip/end? (:loc trio-map)))

(defn extract-route
  "This makes the assumption that we're sitting on the first node of a route
  therefore the next 3 nodes (including the current one,) are the params to
  construct a compojure route.
  
  Use with care and only when it's absolutely certain that we're sitting on
  a route, otherwise bad things will happen."
  [loc]
  ;;; TODO: Ensure that we're sitting on a route.
  (next-nodes loc 3))

(defn route-has-children?
  [loc]
  (> (count (zip/node (zip/up loc))) 3))

(defn append-move
  "Appends an item to the tree at the current level and moves to that location."
  [loc item]
  ;;; If we're at the top, we must insert a node to start with, then we can
  ;;; consistantly use insert-right.
  (zip/down (zip/append-child loc item)))

(defn make-trio
  [loc rt uri]
  {:loc loc :routes rt :uri uri})

(defn make-new-trio
  [definition]
  (make-trio
    (first-item (zip/vector-zip definition))
    (append-move (zip/vector-zip []) [])
    []))

(defn ordered-trio
  [trio-hash]
  (vec (map #(% trio-hash) [:loc :routes :uri])))

(defn cons-route
  [method uri route-fn]
  (compojure/make-route
    ;;; rewrite :any to nil for compojure.
    (if (any-http-method? method)
      nil method)
    uri
    route-fn))

(defn assemble-uri
  [base-uri-vector current-uri]
  (str (apply str base-uri-vector) current-uri))

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
  [loc base-uri]
  (apply cons-route (realize-route base-uri (extract-route loc))))

(defn zipper-level-empty?
  [loc]
  (not (or (zip/down loc)
           (zip/left loc)
           (zip/right loc))))

(defn empty-branch?
  "Returns true if the zipper is sitting on a branch without any children."
  [loc]
  (and (zip/branch? loc)
       (nil? (zip/down loc))))

(defn append-branch-move
  "Append a new branch (empty vector) to the rightmost position of the current location
  which must be a branch already. If we're not appending to the top, dive in (down)."
  [loc]
  (when (nil? loc)
    (throw (Exception. "zipper can not be nil.")))
  (when (not (zip/branch? loc))
    (throw (Exception. "loc not on branch. Must append to a branch.")))
  (zip/rightmost
    (let [added-branch (zip/append-child loc [])]
      (if (not (empty-branch? added-branch))
        (zip/down added-branch)
        added-branch))))

(defn append-route
  [loc route]
  (let [r (zip/append-child loc route)]
    r))

(defn insert-right-move
  [loc item]
  (zip/right (zip/insert-right loc item)))

(defn add-move
  "Adds a new item to the end the current location of the zipper and jumps into it.
  When the zipper is empty, we must append first. All subsequent additions can be inserts."
  [loc item]
  (when (nil? loc)
    (throw (Exception. "nil zipper")))
  (let [new-zipper
        (if (zipper-level-empty? loc)
          (do
            (append-move loc item))
          (do
            (insert-right-move loc item)))]
    new-zipper
    ))

(defn down
  "Moves down in both zippers while creating deeper levels on the working-routes
  that match the zipper as it is traversed.

  NOTICE: Nothing happens to the URI until the next level is processed."
  ([trio-map]
   (apply down (ordered-trio trio-map)))
  ([zipper working-routes uri]
   
   (let [new-working-routes (append-branch-move working-routes)]
     (make-trio
       (zip/down zipper)
       new-working-routes
       uri))))

(defn up
  "Moves up on both zippers at the same time and drops the last uri segment off of
  the uri vector."
  ([trio-map]
   (apply up (ordered-trio trio-map)))
  ([zipper working-routes uri]
   (make-trio
     (zip/up zipper)
     (if (empty-branch? working-routes)
       (zip/remove working-routes)
       (if (nil? (zip/up working-routes))
         working-routes
         (zip/up working-routes)))
     (vec (butlast uri)))))

(defn next-item
  "As we move to the next item, we're moving our working-routes location as well.
  If the trio isn't returned and nil is, then there are no more items."
  ([loc working-routes working-uri]
   (next-item (make-trio loc working-routes working-uri)))

  ([trio-map]
   (when (nil? trio-map)
     (throw (Exception. "trio-map can not be nil")))
   (debug "Finding next item...")
   (loop [state trio-map]
     (let [loc (:loc state)
           routes (:routes state)
           uri (:uri state)]

       (if (vector? (zip/node loc))
         ;;; We can step into it and call it a day.
         (do
           (debug "Found next item.")
           (down state))

         ;;; Otherwise we need to go right or up.
         ;;; Can we go right?
         (if-let [zip-right (zip/right loc)]
           (do
             (recur (assoc state :loc zip-right)))

           ;;; Since we can't move right, we must go up the tree until we can
           ;;; move right. If we reach the top of the tree with no nodes to the
           ;;; right, we're done.

           (if (zip/up loc)
             (do
               (let [new-state (up state)]
                 ;;; We remove the last vector we were on so we don't try to drive
                 ;;; back into it again when we loop through the structure again.
                 (recur (assoc new-state :loc (zip/replace (:loc new-state) nil)))))
             (do
               (debug "There is no next item. We're done.")
               nil))))))))

(defn process-route
  [trio-map]
  (let [new-route (assemble-route (:loc trio-map) (:uri trio-map))
        new-working-routes (append-route (:routes trio-map) new-route)
        new-base-uri (conj
                       (:uri trio-map)
                       (second (zip/node (zip/up (:loc trio-map)))))]
    (make-trio
      (:loc trio-map)
      new-working-routes
      new-base-uri)))

(defn process-context
  [trio-map]
  (let [definition-zipper (:loc trio-map)
        working-routes (append-branch-move (:routes trio-map))
        base-uri (conj (:uri trio-map)
                       (second (zip/node (zip/up definition-zipper))))]
    (make-trio definition-zipper working-routes base-uri)))

(defn process-current
  [trio-map]
  (let [definition-zipper (:loc trio-map)]
    (cond
      (on-route? definition-zipper) (process-route trio-map)
      (on-context? definition-zipper) (process-context trio-map)
      ;;; TODO: Setup middleware.
      ;(on-middleware?) (process-middleware trio-map)
      :else (debug "Bolding doing what we've never done before!")
      )))

