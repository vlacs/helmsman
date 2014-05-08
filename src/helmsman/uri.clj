(ns helmsman.uri
  (:require [clojure.string :as string]))

(defn strip-leading-slashes
  "Removes all leading slashes from the uri string."
  [uri-string]
  (string/replace uri-string #"^/+" ""))

(defn strip-trailing-slashes
  "Removes all trailing slashes from the uri string."
  [uri-string]
  (string/replace uri-string #"/+$" ""))

(defn singular-slashes
  "Replaces more than one consecutive slashes with a single one."
  [uri-string]
  (string/replace uri-string #"/+" "/"))

(defn trim-slashes
  "Removes all slashes from the beginning and end of the uri string."
  [uri-string]
  (-> uri-string
      strip-leading-slashes
      strip-trailing-slashes))

(defn path
  "Creates a flat URI path to be used for navigation."
  [uri-string]
  (string/split
    (-> uri-string
        trim-slashes
        singular-slashes)
    #"/+"))

(defn wildcard-string?
  [identifier]
  (= (str (first identifier)) "*"))

(defn variable-string?
  [identifier]
  (or (when (string? identifier)
        (= (str (first identifier)) ":"))
      (keyword? identifier)))

(defn normalize-path
  "Converts a multi-level uri-path vector into a single level vector and
  removes any empty items. This is useful for path navigation and uri
  generation."
  [uri-path]
  (vec 
    (filter 
      (fn normalize-path-filter-fn
        [i]
        (if (keyword? i)
         true
         (not (empty? i)))) (flatten uri-path))))

(defn keywordize
  [i]
  (keyword (apply str (rest i))))

(defn sub-path-item
  [sub-map i]
  (if (variable-string? i)
    (get sub-map
         (if (not (keyword? i))
           (keywordize i) i) i) i))

(defn process-path-args
  [uri-path args]
  (map (partial sub-path-item args) uri-path))

(defn assemble
  "Turns a uri path into a uri string.
  TODO: Handle uri arguments"
  [uri-path & args]
  (apply
    str
    (interpose
      "/"
      (process-path-args
        (normalize-path uri-path)
        (if (empty? args)
          {}
          (apply
            assoc
            {}
            args))))))

(defn common-path
  [uri-one uri-two]
  (loop [common-uri []
         one (normalize-path uri-one)
         two (normalize-path uri-two)]
    (let [s1 (first one)
          s2 (first two)]
      (if (or (not (= s1 s2))
              (nil? s1)
              (nil? s2))
        common-uri
        (recur
          (conj common-uri s1)
          (vec (rest one))
          (vec (rest two)))))))

(defn path-divergence
  "Returns two uri paths with the common preceeding URI paths stripped out.
  These paths are relative to each other."
  [uri-one uri-two]
  (loop
    [one (normalize-path uri-one)
     two (normalize-path uri-two)
     last-segment nil]
    (let [s1 (first one)
          s2 (first two)]
      (if
        (or
          (and
            (not (= s1 s2))
            (not (keyword? s1))
            (not (keyword? s2)))
          (nil? s1)
          (nil? s2))
        [one two] 
        (recur
          (vec (rest one))
          (vec (rest two))
          s2)))))

(defn relative-uri
  [from-path to-path]
  (let [divergence (path-divergence from-path to-path)
        u-levels (- (count (first divergence)) 1)]
    (if (<= u-levels 0)
      to-path
      (into
        (vec (repeat u-levels ".."))
        (second divergence)))))

(defn relative-uri-str
  "Simplifies relative URI creation. Given a ring request map and a
  destination uri path, we can make a relative URI string, even with named
  uri parameters. The caviat is that we can't create a URI to a path with
  a wildcard. It's too ambiguous and we discourage it by not supporting it."
  [request destination-uri-path & assemble-fn-args]
  (apply
    (partial assemble
             (relative-uri
               (get-in request [:helmsman :uri-path])
               destination-uri-path))
    assemble-fn-args))
