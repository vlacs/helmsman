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
        length (count item)]
    {:context? context?
     :route? route?
     :middleware? (routes/middleware? hpi)
     :path (when (contains? routes/path-bearing-keywords hpi)
             (uri/path spi))
     :route-fn (when route? (nth item 2))
     :middleware-fn nil
     :nested (when 
               (contains? routes/nestable-keywords hpi)
               (let [di (get routes/keyword-route-length hpi)]
                 (when (> length di)
                   ;;; This is where this call becomes recursive.
                   (about-vector
                     (subvec item di)))))}))

(defn about-vector
  "Takes a definition list and maps about-stanza on it."
  [definition-list]
  (map about-stanza definition-list))
