(ns helmsman.tree
  (:require [clojure.zip :as zip]
            [taoensso.timbre :as timbre]
            [helmsman.routes :as routes]
            [helmsman.uri :as uri]
            [helmsman.middleware :as middleware]))
(timbre/refer-timbre)

(defn on-route?
  "Assuming the zipper is sitting on the first element of the current vector,
  the first node (the one we're on,) will be checked to see if this item
  reflects that of a helmsman/compojure route."
  [zipper]
  (routes/route? (zip/node zipper)))

(defn on-context?
  "Assuming the zipper is sitting on the first element of the current vector,
  the first node we're on will be checked to see if the item reflects a
  helmsmen context which behaves identically as a compojure context."
  [zipper]
  (routes/context? (zip/node zipper)))

(defn on-middleware?
  "see on-route?
  Returns true if the zipper is sitting on a middleware item."
  [zipper]
  (routes/middleware? (zip/node zipper)))

(defn on-resources?
  "Checks to see if we're on a :resources directive. These are static files served
  up from the classpath."
  [zipper]
  (routes/resources? (zip/node zipper)))

(defn on-files?
  "Checks to see if we're on a :files directive. These are static files served up
  from any path on the current computer or server."
  [zipper]
  (routes/files? (zip/node zipper)))
 
(defn on-uri?
  "Assuming the zipper is sitting on the first element of the current vector,
  the first node will be checked to see if we're sitting on a node that supports
  URIs such as contexts and routes."
  [zipper]
  (or (on-route? zipper)
      (on-context? zipper)
      (on-resources? zipper)
      (on-files? zipper)))

(defn flatten-all-routes
  "Reduces the tree of handlers to a single level vector to be processed by
  compojure.core/routes"
  [trio-map]
  (vec (flatten (zip/root (:routes trio-map)))))

(defn flatten-current-routes
  [routes-zipper]
  (vec (flatten (zip/node routes-zipper))))

(defn replace-current-routes
  "Keep in mind that while we're working on a route (particularly with 
  middleware) we're replacing everything, which includes an empty vector
  that represents the location of the next potential child."
  [routes-zipper new-item]
  (zip/replace routes-zipper [new-item]))

(defn first-item
  "Moves the zipper to the very first item (route) that is encountered.
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

(defn extract-route
  "This makes the assumption that we're sitting on the first node of a route
  therefore the next 3 nodes (including the current one,) are the params to
  construct a compojure route.
  
  Use with care and only when it's absolutely certain that we're sitting on
  a route, otherwise bad things will happen."
  [loc]
  ;;; TODO: Ensure that we're sitting on a route.
  (next-nodes loc 3))

(defn extract-uri
  [loc]
  (when (not (on-uri? loc))
    (throw (Exception. "Can't extract URI from an item that doesn't support having a URI")))
  (uri/path (zip/node (zip/right loc))))

(defn extract-middleware-fn
  "This is only a call to zip/node. We're running under the assumption that the
  zipper is always sitting on the left-most node on this level. This fn call is
  just to reinforce the fact that we're making this assumption."
  [loc]
  (zip/node loc))

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
  "loop-recur is utilized to traverse the zippers and working uri. This puts it into
  an easy to use map for us to pass around."
  [loc rt uri-vector]
  {:loc loc :routes rt :uri uri-vector})

(defn make-new-trio
  "see: make-trio
  This makes an initial trio from a definition vector tree."
  [definition]
  (make-trio
    (first-item (zip/vector-zip definition))
    (append-move (zip/vector-zip []) [])
    []))

(defn ordered-trio
  "see: make-trio
  Some fns require the trio to be individual arguments in fns, this preps the
  trio to be apply(ed) to a fn. Returns a vector representation of the map in
  the order of definition zipper, routes zipper, uri vector."
  [trio-hash]
  (vec (map #(% trio-hash) [:loc :routes :uri])))

(defn zipper-level-empty?
  "Checks to see if this zipper level is completely empty.
  (Cannot move left, right, or down. We don't care if we can move up in this fn.)"
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
    (throw (Exception. (str "loc not on branch. Must append to a branch.\n"
                            (prn-str loc)
                            ))))
  (zip/rightmost
    (let [added-branch (zip/append-child loc [])]
      (if (not (empty-branch? added-branch))
        (zip/down added-branch)
        added-branch))))

(defn append-route
  "Appends a route to the route zipper at its current location."
  [loc route]
  (zip/append-child loc route))

(defn insert-right-move
  "Inserts a node to the right of the current zipper location and moves the zipper to that
  new node."
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
          (append-move loc item)
          (insert-right-move loc item))]
    new-zipper))

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

(defn rights-vec
  [zipper]
  (if-let [i (zip/rights zipper)]
    (vec i) []))

(defn next-item
  "As we move to the next item, we're moving our working-routes location as well.
  If the trio isn't returned and nil is, then there are no more items."
  ([loc working-routes working-uri]
   (next-item (make-trio loc working-routes working-uri)))

  ([trio-map]
   (when (nil? trio-map)
     (throw (Exception. "trio-map can not be nil")))
   (loop [state trio-map]
     (let [loc (:loc state)
           routes (:routes state)]
       (if (and (zip/branch? loc) (zip/down loc))
         ;;; We can step into it and call it a day.
         (down state)
         ;;; Otherwise we need to go right or up.
         ;;; Can we go right?
         (if-let [zip-right (zip/right loc)]
           (recur (assoc state :loc zip-right))
           ;;; Since we can't move right, we must go up the tree until we can
           ;;; move right. If we reach the top of the tree with no nodes to the
           ;;; right, we're done.
           (if (zip/up loc)
             (let [new-state (up state)]
               ;;; We remove the last vector we were on so we don't try to drive
               ;;; back into it again when we loop through the structure again.
               (recur (assoc new-state :loc (zip/replace (:loc new-state) nil))))
               nil)))))))

(defn process-route
  "Handles a route inside the routes definition."
  [trio-map]
  (let [new-base-uri (conj
                       (:uri trio-map)
                       (extract-uri (:loc trio-map)))
        new-route (middleware/attach-meta
                    (apply
                      routes/cons-route
                      (routes/rewrite-uri
                        (extract-route (:loc trio-map))
                        (str "/" (uri/assemble new-base-uri))))
                    (meta (zip/node (zip/up (:loc trio-map)))))
        new-working-routes (append-route (:routes trio-map) new-route)]
    (make-trio
      (:loc trio-map)
      new-working-routes
      new-base-uri)))

(defn process-context
  "Handles a context and uri without a route in the routes definition."
  [trio-map]
  (let [definition-zipper (:loc trio-map)
        working-routes (append-branch-move (:routes trio-map))
        base-uri (conj (:uri trio-map)
                       (extract-uri definition-zipper))]
    (make-trio definition-zipper working-routes base-uri)))

(defn process-middleware
  "Handles the processing of a middleware in the routes definition."
  [trio-map]
  (let [middlware-fn (extract-middleware-fn (:loc trio-map))
        middleware-args (rights-vec (:loc trio-map))
        route-level-zipper (zip/up (:routes trio-map))
        flattened-routes (flatten-current-routes route-level-zipper)]
    ;;; BEWARE! The routes zipper is not sitting on the level that we're working
    ;;; with. We must go up, do some stuff, then drop back in.
    (make-trio
      (zip/replace (zip/up (:loc trio-map)) [])
      (replace-current-routes
        route-level-zipper
        (apply
          (partial
            middlware-fn
            (apply routes/combine
                   (flatten-current-routes
                     route-level-zipper)))
          middleware-args))
      (:uri trio-map))))

(defn process-static
  [trio-map static-fn]
  (let [contents (rights-vec (:loc trio-map))
        uri (conj (:uri trio-map) (uri/path (first contents)))
        opts (rest contents)]
    (make-trio
      (zip/replace (zip/up (:loc trio-map)) [])
      (append-route (:routes trio-map)
                    (static-fn
                      (str "/" (uri/assemble
                                 (uri/normalize-path uri)))
                      opts))
      (:uri trio-map))))

(defn process-current
  "Processes the current item that the location zipper is sitting on.
  Note: It is expected that the zipper is sitting on the first node of
  the route definition."
  [trio-map]
  (let [definition-zipper (:loc trio-map)]
    (cond
      (on-route? definition-zipper) (process-route trio-map)
      (on-context? definition-zipper) (process-context trio-map)
      (on-middleware? definition-zipper) (process-middleware trio-map)
      (on-resources? definition-zipper) (process-static trio-map routes/cons-resources)
      (on-files? definition-zipper) (process-static trio-map routes/cons-files)

      ;;; TODO: Think up a better debug message. Maybe include some data too.
      :else (debug "Bolding doing what we've never done before!")
      )))

(defn gather-all-meta
  "Gets metadata about the helmsman definition."
  [site-structure]
  (loop [state (make-new-trio site-structure)
         nav-set #{}]
    (if (nil? state)
      nav-set
      (let [processed-state (process-current state)
            loc (:loc processed-state)]
        (recur
          (next-item processed-state)
          (if
            (on-uri? loc)
            (conj
              nav-set
              (assoc
                (meta (zip/node (zip/up loc)))
                :uri-path
                (uri/normalize-path (:uri processed-state))))
            nav-set))))))

