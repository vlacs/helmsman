(ns helmsman.router
  (:require [helmsman.uri :as uri]
            [helmsman.routes :as routes]
            [helmsman.request :as h-request]))

(defn about-stanza
  "Takes a helmsman definition stanza and turns it into something more useful
  that describes what the stanza says and facilities for accessing the
  different portions of any given stanza. Any nested stanzas will be in the
  :nested item of the map, which can be used to recursively generate stanzas."
  [item]
  (if (nil? item)
    nil
    (let [hpi (first item)
          spi (second item)
          context? (routes/context? hpi)
          route? (routes/route? hpi)
          middleware? (routes/middleware? hpi)
          length (count item)
          min-length
          (get
            routes/keyword-route-length
            hpi length)]
      {:context? context?
       :route? route?
       :http-method (when route? hpi)
       :middleware? middleware?
       :path (when (contains? routes/path-bearing-keywords hpi)
               (uri/path spi))
       :route-fn (when route? (nth item 2))
       :raw-item item
       :meta (meta item)
       :sub-definition 
       (when (and
               (or route? context?)
               (> length min-length))
         (subvec item min-length))})))

(defn ordered-middleware
  [middleware-stack]
  (reduce
    (fn [w c]
      (if (empty? c)
        w
        (apply (partial conj w) c)))
    (list)
    middleware-stack))

(defn make-signature-matcher-fn
  [signature]
  (fn [i]
    (loop [x signature
           y i]
      (let [empty-x? (empty? x)
            empty-y? (empty? y)]
        (if
          (and empty-x? empty-y?)
          true
          (let [fx (first x)
                fy (first y)]
          (if
            (or empty-x?
                empty-y?
                (and
                (not= fx \?)
                (not= fy \?)
                (not= fx fy)))
            false
            (recur
              (rest x)
              (rest y)))))))))

(defn make-path-matcher-fn
  [constant-path]
  (fn [other-path]
    (let [pd (uri/path-divergence constant-path other-path)]
      (and (empty? (first pd)) (empty? (second pd))))))

(defn make-route-fn
  [handler-fn middleware-list]
  (loop [handler-state handler-fn
         middlewares (reverse middleware-list)]
    (if (empty? middlewares)
      handler-state
      (let [this-middleware (first middlewares)
            mw-fn (first this-middleware)
            mw-args (vec (rest this-middleware))
            mw-beginning [mw-fn handler-state]]
        (recur
          (apply
            (partial mw-fn handler-state)
            mw-args)
          (rest middlewares))))))

(defn make-route
  [http-method path handler-fn middleware meta-data]
  (let [real-path (uri/normalize-path (reverse path))
        path-param-positions (uri/path-param-positions real-path)
        signature (uri/path->signature real-path)
        middleware (ordered-middleware middleware)]
    {:http-method http-method
     :path real-path
     :path-param-positions (uri/path-param-positions real-path)
     :signature signature
     :middleware middleware 
     :handler-fn handler-fn
     :full-route-fn (make-route-fn handler-fn middleware)
     :meta meta-data
     :id (:id meta-data)
     :signature-matcher-fn (make-signature-matcher-fn signature)
     :path-matcher-fn (make-path-matcher-fn real-path)}))

(defn process-compiled-routes
  [compiled-routes
   current-stanza
   stacked-paths
   stacked-middleware]
  (if (:route? current-stanza)
    (conj
      compiled-routes
      (make-route
        (:http-method current-stanza)
        (conj stacked-paths (:path current-stanza))
        (:route-fn current-stanza)
        stacked-middleware
        (:meta current-stanza)))
    compiled-routes))

(defn process-stacked-routes
  [current-stanza upcoming-routes stacked-routes]
  (if (nil? current-stanza)
    (pop stacked-routes)
    (if (:sub-definition current-stanza)
      (conj stacked-routes (rest upcoming-routes))
      stacked-routes)))

(defn process-stacked-paths
  [current-stanza stacked-paths]
  (if (nil? current-stanza)
    (pop stacked-paths)
    (if (:sub-definition current-stanza)
      (conj stacked-paths (:path current-stanza))
      stacked-paths)))

(defn process-upcoming-routes
  [current-stanza upcoming-routes stacked-routes]
  (if (nil? current-stanza)
    (first stacked-routes)
    (if-let [new-level (:sub-definition current-stanza)]
      new-level (rest upcoming-routes))))

(defn process-stacked-middleware
  [current-stanza stacked-middleware]
  (if (nil? current-stanza)
    (pop stacked-middleware)
    (if (:middleware? current-stanza)
      (conj
        (pop stacked-middleware)
        (conj
          (first stacked-middleware)
          (:raw-item current-stanza)))
      (if (not (nil? (:sub-definition current-stanza)))
        (conj stacked-middleware (list))
        stacked-middleware))))

(defn destruct-definition
  "The core of Helmsman's integrated routing. Converts a Compojure like routing
  definition into a Helmsman route map which describes each and every route in
  its entirety, including the middleware that need to run for any route to
  enable the ability to route before middleware and to route quickly based on
  Helmsman's URI handling."
  [definition]
  (set
    (loop
      [compiled-routes (list)
       cl-upcoming-routes definition
       stacked-routes (list)
       stacked-paths (list)
       stacked-middleware (list (list))]
      (if
        (and
          (empty? stacked-routes)
          (empty? cl-upcoming-routes))
        compiled-routes
        (let [current-stanza (about-stanza (first cl-upcoming-routes))]
          (recur
            (process-compiled-routes
              compiled-routes current-stanza
              stacked-paths stacked-middleware)
            (process-upcoming-routes
              current-stanza cl-upcoming-routes
              stacked-routes)
            (process-stacked-routes
              current-stanza cl-upcoming-routes stacked-routes)
            (process-stacked-paths
              current-stanza stacked-paths)
            (process-stacked-middleware
              current-stanza stacked-middleware)))))))

(defn create-ring-handler
  [definition]
  (let [routing-set (destruct-definition definition)]
    (fn ring-handler
      [request]
      (let [request (h-request/default-wrappers request routing-set)]
        ((get-in request [:helmsman :current-route :full-route-fn]
                 ;;; Figure out a better 404 method than this.
                 (constantly {:status 404 :body "Not found."}))
         request)))))

