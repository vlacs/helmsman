(ns helmsman.navigation.preds)

(defn has-a?
  "Creates a predicate fn that returns true if a map is passed in (i) that contains
  a key (meta-map-key) and is a not false or nil."
  ([meta-map-key]
   (fn exists?-fn
     [i]
     (and (contains? i meta-map-key)
          (if (get i meta-map-key)
            true
            false)))))

(defn with?
  "Creates a predicate fn that returns true if a map (i) contains a key (k) with
  the value (v)."
  [k v]
  (fn [i]
    (if (map? i)
      (= v (get i k))
      false)))

;;; TODO: Get rid of this and just use 'with?'. --jdoane
(defn has-id?
  "Predicate that check to see if the :id key has a value (id).
  See the 'with?' fn for more information."
  [id]
  (with? :id id))

(defn main-menu?
  "Checks to see if the :main-menu key was set on the map passed in to the generated
  predicate from has-a?.
  See the 'has-a?' fn for more information."
  []
  (has-a? :main-menu))

