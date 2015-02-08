(ns helmsman.request
  (:require
    [helmsman.uri :as uri]
    [ring.middleware.params]))

(defn prepare-keys
  [i]
  (vec
    (concat
      [:helmsman]
      (if (keyword? i)
        [i] i))))

(defn wrap-attribute
  "assoc(-in)s an attribute within the :helmsman portion of the request.
  The attribute name can be a string, in which case it acts like assoc or it
  can be a vector/seq and be treated as an assoc-in conj'ed to [:helmsman]."
  [request attribute-name attribute-value]
  (assoc-in
    request
    (prepare-keys attribute-name)
    attribute-value))

(defn ensure-wrapped
  "Makes sure a key is set. If it isn't, it is set using the fn provided
  against the request. It always returns the request with the key, provided
  the wrapper returns a non-nil value. wrapper-fn take 1 arg; the request."
  [request attribute-name wrapper-fn]
  (let [key-list (prepare-keys attribute-name)]
    (if (nil? (get-in request key-list))
      (assoc-in request key-list (wrapper-fn request))
      request)))

(defn wrap-path
  "Attaches the helmsman uri path to the request."
  [request]
  (wrap-attribute request :request-path (uri/path (:uri request))))

(defn wrap-signature
  "Wraps the request path signature the helmsman portion of the requestion. It
  also ensures that the wrap-path has been run on the request as well. Since
  it's required, there is no reason to get rid of it if it isn't there already."
  [request]
  (let
    [request (ensure-wrapped request :request-path wrap-path)] 
    (wrap-attribute
      request :request-signature
      (uri/path->signature (get-in request [:helmsman :request-path])))))

(defn wrap-routing-set
  "Wraps the routing set into the request to be used by the handler and to
  do the route matching without wasting that data."
  [request routing-set]
  (wrap-attribute request :routing-set routing-set))

(defn wrap-current-route
  "This is the last step to having everything we need to run the handler in
  Helmsman land. This requires the routing set and signature to be wrapped."
  [request]
  (wrap-attribute
    request :current-route
    (let [request-path (get-in request [:helmsman :request-path])]
      (some
        (fn [i]
          (when
            (and
              ((:path-matcher-fn i) request-path)
              (= (:request-method request) (:http-method i)))
            i))
        (get-in request [:helmsman :routing-set])))))

(defn default-wrappers
  "Makes changes to the request to make routing easier and to include data that
  could be useful to the developer, such as creating relative URIs, or even
  programatically describing a context of routes using meta-data. The idea that
  this only occurs once, when the request is coming in. It shouldn't be a
  middleware because we don't care about the intput."
  [request routing-set]
  (-> request
      wrap-path
      wrap-signature
      (wrap-routing-set routing-set)
      wrap-current-route
      wrap-uri-params
      ))

(defn get-in-request
  [request key-or-keys]
  (get-in
    request 
    (concat
      [:helmsman]
      (if (keyword? key-or-keys)
        [key-or-keys] key-or-keys))))

(defn get-current-route
  [request]
  (get-in-request request :current-route))

(defn get-routing-set
  [request]
  (get-in-request request :routing-set))

(defn nice-keys
  [set-index single-key?]
  (if single-key?
    (into
      {}
      (map
        (fn [i] [(second (first (first i))) (second i)])
        set-index))
    set-index))

(defn index-routes
  [routing-set index-on]
  (let [single-key? (keyword? index-on)]
    (-> routing-set
        (clojure.set/index
          (if single-key? [index-on] index-on))
        (nice-keys single-key?))))

(defn get-index-routes
  "Direct request wrapper for index-routes."
  [request index-on]
  (index-routes (get-routing-set request) index-on))
 
