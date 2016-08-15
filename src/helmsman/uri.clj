(ns helmsman.uri
  (:require [clojure.string :as string]))

(defn clean-uri-string
  "Removes all slashes from the beginning and end of the uri string."
  (^String [^String uri-string]
   ;;; TODO: Convert this to a non-regex version.
   (apply
     str
     (let [uri-vector (vec uri-string)
           uri-size (count uri-vector)]
       (loop
         [new-segment? false
          new-string []
          pos 0]
         (let [empty-new-string? (empty? new-string)
               char-current (char (get uri-vector pos \0))
               char-current-is-slash? (= char-current \/)]
           (if (>= pos uri-size)
             (if new-segment?
               (conj new-string "/") new-string)
             (recur
               char-current-is-slash?
               (if empty-new-string?
                 (if char-current-is-slash?
                   new-string
                   (conj new-string char-current))
                 (if new-segment?
                   (if (not char-current-is-slash?)
                     (conj new-string \/ char-current)
                     new-string)
                   (if char-current-is-slash?
                     new-string
                     (conj new-string char-current))))
               (inc pos)))))))))

(defn uri-split-slashes
  (^clojure.lang.PersistentVector
    [^String cleaned-uri-string]
   (let [uri-vector (vec cleaned-uri-string)
         uri-size (count cleaned-uri-string)]
     (if (<= uri-size 0) []
       (loop
         [current-segment []
          uri-segments []
          pos 0]
         (let [char-current (char (get uri-vector pos \0))
               char-current-is-slash? (= char-current \/)]
           (if (>= pos uri-size)
             (conj uri-segments (apply str current-segment))
             (recur
               (if char-current-is-slash?
                 []
                 (conj current-segment char-current))
               (if char-current-is-slash?
                 (conj uri-segments (apply str current-segment))
                 uri-segments)
               (inc pos)))))))))

(defn variable-string?
  [identifier]
  (or (when (and (string? identifier) (not (empty? identifier)))
        (= (char (first identifier)) \:))
      (keyword? identifier)))

(defn keywordize
  [^String i]
  (keyword (apply str (rest i))))

(defn transform-keywords
  [uri-path]
  (vec
    (map
      (fn [i] (if (variable-string? i) (keywordize i) i))
      uri-path)))

(defn path
  "Creates a flat URI path to be used for navigation."
  (^clojure.lang.PersistentVector
    [^String uri-string]
    (transform-keywords
      (uri-split-slashes
        (clean-uri-string uri-string)))))

(defn normalize-path
  "Converts a multi-level uri-path vector into a single level vector and
  removes any empty items. This is useful for path navigation and uri
  generation."
  (^clojure.lang.PersistentVector
    [^clojure.lang.PersistentVector uri-path]
    (filterv 
      (fn normalize-path-filter-fn
        [i]
        (or (keyword? i) (string? i)))
      (flatten uri-path))))

(defn sub-path-item
  [sub-map i]
  (if (and (not (empty? i)) (variable-string? i))
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
        common (common-path from-path to-path)
        u-levels (- (count (first divergence)) 1)]
    (vec
      (concat
        (if (<= u-levels 0)
          (concat
            ["."]
            (if (and (= u-levels -1)
                     (> (count common) 0))
              [(last common)] []))
          (vec (repeat u-levels "..")))
        (second divergence)))))

(defn signature-map-fn
  [i]
  (if (keyword? i)
    \? (first i)))

(defn path->signature
  [path]
  (if (empty? path)
    '(nil) (map signature-map-fn path)))

(defn path-param-positions
  "Takes in a definition path and maps integer vector positions with url
  parameter names."
  [defined-path]
  (let [path-max-idx (- (count defined-path) 1)]
    (loop
      [pos 0
       working-map {}]
      (if (> pos path-max-idx)
        working-map
        (let [current-item (get defined-path pos)]
          (recur
            (inc pos)
            (if (keyword? current-item)
              (assoc working-map pos current-item)
              working-map)))))))

(defn extract-path-params
  [request-path param-positions]
  (into
    {}
    (map
      (fn [i]
        (let [pos (first i)
              param-name (second i)]
          [param-name (get request-path pos)]))
      param-positions)))

