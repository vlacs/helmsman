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
        length (count item)
        min-length
        (get
          routes/keyword-route-length
          hpi length)]
    {:context? context?
     :route? route?
     :middleware? (routes/middleware? hpi)
     :path (when (contains? routes/path-bearing-keywords hpi)
             (uri/path spi))
     :route-fn (when route? (nth item 2))
     :middleware-fn nil
     :sub-definition 
     (when (and
             (route? or context?)
             (> length min-length))
       (subvec item min-length))}))

(defn make-route
  [http-method path middleware meta-data]
  {:http-method http-method
   :path path
   :signature 
   :middleware middleware
   :handler-fn handler-fn
   :meta meta-data})

(defn destruct-definition
  "The core of Helmsman's integrated routing. Converts a Compojure like routing
  definition into a Helmsman route map which describes each and every route in
  its entirety, including the middleware that need to run for any route to
  enable the ability to route before middleware and to route quickly based on
  Helmsman's URI handling."
  [definition]
  (loop [route-seq '()
         current-stanza (about-stanza (first definition))
         middleware-stack []
         return-stack (subvec definition 1)]
    (if (empty? return-stack) and (empty? (:sub-definition current-stanza))
      
      )
    ))

