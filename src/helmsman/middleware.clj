(ns helmsman.middleware
  (:require [helmsman.uri :as uri]
            [taoensso.timbre :as timbre]))
(timbre/refer-timbre)

(defn attach-logger
  "Creates middleware that logs when a request comes in."
  [handler]
  (fn attach-helmsman-logger-middleware-fn
    [request]
    (info (:remote-addr request)
          " requesting "
          (:uri request)
          " via " 
          (:request-method request))
    (handler request)))

(defn attach-meta
  [handler meta-data]
  (fn attach-meta-middleware-fn
    [request]
    (handler
      (assoc-in request [:helmsman :route-meta] meta-data))))

(defn attach-helmsman
  "Attaches helmsman specific data to the request."
  [handler all-meta-data]
  (fn attach-helmsman-middleware-fn
    [request]
    (handler
      (assoc
        request
        :helmsman
        ;;; Unsure if all-meta is needed. We can determine this at any time.
        {:all-meta all-meta-data
         :uri-path (uri/path (:uri request))}))))

