(ns helmsman.test.routing
  (:require
    [helmsman.routes]
    [helmsman.test.uri]
    [clojure.test.check :as tc]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop]))

(defn example-sucessful-response
  [request]
  {:status 200
   :body "Ok."})

(def http-method-gen
  (gen/elements
    (vec helmsman.routes/http-methods)))

(def route-gen
  (gen/tuple http-method-gen helmsman.test.uri/path-string-gen
             (gen/elements [example-sucessful-response])))

