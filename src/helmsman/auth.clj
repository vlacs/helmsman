(ns helmsman.auth
  (:require
    [ring.middleware.cookies]
    [ring.middleware.session]))

;;; This is designed to be a replacement for "Friend" that integrates nicely
;;; with Helmsman by using meta-data to store role requirements with
;;; a simplified workflow design.

(defn make-auth-map
  [restore-user-fn]
  {:session-cookie-key "helmsman-cookiekey"
   :user
   {:authenticated-by nil
    :current nil
    :masquerade nil}
   :restore-user-fn nil
   :valid-login-fn nil
   :login
   {:redirect-url nil}})

(defn session-restore
  "Goes through the request and attempts to retrieve the user it is associated
  with. It will leave a nil in current-user if no user is logged in."
  [request]
  (let [session (:session request)]
    (assoc-in
      request
      [:helmsman :auth]

      )
    )
  nil
  )

(def required-middleware
  [[ring.middleware.cookies/wrap-cookies]
   [ring.middleware.session/wrap-session]])

(defn auth-manager-middleware
  "Configures the Ring request with authentication. This loads a user who may
  be already logged in or sets the stage for a non-authenticated user."
  [handler restore-session-fn]
  (fn [request]
    (handler
      (restore-session-fn request))))
