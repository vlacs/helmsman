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

(defn assemble
  "Turns a uri path into a uri string."
  [uri-path]
  (apply (partial str "/") (interpose "/" (vec (filter #(not (empty? %1)) (flatten uri-path))))))
