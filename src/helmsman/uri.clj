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

(defn normalize-path
  "Converts a multi-level uri-path vector into a single level vector and
  removes any empty items. This is useful for path navigation and uri
  generation."
  [uri-path]
  (vec (filter #(not (empty? %1)) (flatten uri-path))))

(defn assemble
  "Turns a uri path into a uri string."
  [uri-path]
  (apply (partial str "/") (interpose "/" (normalize-path uri-path))))

(defn common-uri-path
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

(defn relative-uri-path
  [uri-one uri-two]
  (loop
    [one (normalize-path uri-one)
     two (normalize-path uri-two)]
    (let [s1 (first one)
          s2 (first two)]
      (if (or (not (= s1 s2))
              (nil? s1)
              (nil? s2))
        [one two]
        (recur
          (vec (rest one))
          (vec (rest two)))))))
