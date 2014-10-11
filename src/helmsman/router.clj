(ns helmsman.router
  (:require [helmsman.uri :as uri]
            [helmsman.routes :as routes]))

;;; This is needed to make the pair of these functions recursive.
(declare about-stanza about-vector)

(defn about-stanza
  "Takes a helmsman definition stanza and turns it into something more useful
  that describes what the stanza says and facilities for accessing the
  different portions of any given stanza. Any nested stanzas will be in the
  :nested item of the map, which can be used to recursively generate stanzas."
  [item]
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
       (subvec item min-length))}))

(defn signature-map-fn
  [i]
  (if (keyword? i)
    \? (first i)))

(defn make-route
  [http-method path handler-fn middleware meta-data]
  {:http-method http-method
   :path path
   :signature (map signature-map-fn path)
   :middleware middleware
   :handler-fn handler-fn
   :meta meta-data})

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
        (conj stacked-path (:path current-stanza))
        (:route-fn current-stanza)
        stacked-middleware
        (:meta current-stanza)))
    compiled-routes))

(defn process-stacked-routes
  [current-stanza upcoming-routes stacked-routes]
  (if (nil? current-stanza)
    (pop stacked-routes)
    (if (:sub-definition current-stanza)
      (cons (rest upcoming-routes) stacked-routes)
      stacked-routes)))

(defn process-stacked-paths
  [current-stanza stacked-paths]
  (if (nil? current-stanza)
    (pop stacked-paths)
    (if (:sub-definition current-stanza)
      (cons (:path current-stanza) stacked-paths)
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
      (cons
        (cons
          (:raw-item current-stanza)
          (first stacked-middleware))
        (rest stacked-middleware))
      stacked-middleware)))

(defn destruct-definition
  "The core of Helmsman's integrated routing. Converts a Compojure like routing
  definition into a Helmsman route map which describes each and every route in
  its entirety, including the middleware that need to run for any route to
  enable the ability to route before middleware and to route quickly based on
  Helmsman's URI handling."
  [definition]
  (loop
    [compiled-routes '()
     cl-upcoming-routes definition
     stacked-routes '()
     stacked-paths '()
     stacked-middleware '('())]
    (let [level-empty (empty cl-upcoming-routes)]
      (if
        (and
          (empty? stacked-routes)
          (empty? cl-upcoming-routes))
        compiled-routes
        (let [current-stanza (about-stanza (first cl-upcoming-routes))
              after-stanza-items (rest cl-upcoming-routes)
              step-in (not (nil? (:sub-definition current-stanza)))]
          (recur
            (process-compiled-routes
              compiled-routes current-stanza
              stacked-paths stacked-middleware)
            (process-upcoming-routes
              current-stanza cl-upcoming-routes)
            (process-stacked-routes
              current-stanza cl-upcoming-routes stacked-routes)
            (process-stacked-paths
              current-stanza stacked-paths)
            (process-stacked-middleware
              current-stanza stacked-middleware)))))))

