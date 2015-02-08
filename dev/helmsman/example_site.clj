(ns helmsman.example-site
  (:require 
    [ring.adapter.jetty]
    [helmsman :as h]
    [helmsman.router :as router]
    [helmsman.navigation :as nav]
    [helmsman.uri :as uri]
    [ring.util.response]
    [ring.middleware.keyword-params]
    [ring.middleware.cookies]))

(def ring-response ring.util.response/response)

(defn basic-html-doc
  "Creates a basic template for an HTML page."
  [body]
  (str "<!doctype html><html><body>"
       body "</body></html>"))

(defn home-page-body
  "Creates the basic body for a basic navigational page."
  [request]
  (let [add-uri (nav/assemble-relative-uri request ::add
                             :one 4 :two 8)
        sub-uri (nav/assemble-relative-uri request ::subtract
                             :one 3 :two 13)
        mult-uri (nav/assemble-relative-uri request ::multiply
                              :one 23 :two 7)]
    (str "<h1>Welcome to the Helmsman Example!</h1>
         <ul>
         <li><a href=\"" add-uri "\">4 + 8</a></li>
         <li><a href=\"" sub-uri "\">3 - 13</a></li>
         <li><a href=\"" mult-uri "\">23 * 7</a></li>
         </ul>")))

(defn home-page
  "A create a little header to identify that we're at the home page."
  [request]
  (basic-html-doc "<h1>Hello world!</h1>"))

(h/defhandler home-page
  [:as request]
  (ring-response
    (home-page-body
      request)))

(defn punt
  "Converts a string to an integer, basic wrapper."
  [n] (Integer/parseInt n))

(defn return-home
  "Creates an a tag with a link back to the home page."
  [request]
  (str "<a href=\""
       (nav/assemble-relative-uri request ::home)
       "\">Go Home</a>"))

(h/defhandler add-page
  [one two :as request]
  (ring-response
    (str "Result: " (+ (punt one) (punt two))
         "<br />"
         (home-page-body request)
         (return-home request))))

(h/defhandler subtract-page
  [one two :as request]
  (ring-response
    (str "Result: " (- (punt one) (punt two))
         "<br />"
         (home-page-body request)
         (return-home request))))

(h/defhandler multiply-page
  [one two :as request]
  (ring-response
    (str "Result: " (* (punt one) (punt two))
         "<br />"
         (home-page-body request)
         (return-home request))))

(h/defhandler debug-page
  [:as request]
  (ring-response
    (prn-str request)))

(def our-routes
  [[ring.middleware.keyword-params/wrap-keyword-params]
   [ring.middleware.cookies/wrap-cookies]
   ^{:id ::home}
   [:get "/" home-page]
   [:context "math"
    ^{:id ::add}
    [:get "add/:one/:two" add-page]
    ^{:id ::subtract}
    [:get "subtract/:one/:two" subtract-page]
    ^{:id ::multiply}
    [:get "multiply/:one/:two" multiply-page]]
   ^{:id ::debug}
   [:get "/debugging/:a/:b" debug-page]
   ])

(defn start-server
  [port definition]
  (ring.adapter.jetty/run-jetty
    (helmsman/create-ring-handler definition)
    {:port port
     :join? false}))

(comment  
  (use 'clojure.repl 'clojure.pprint)
  (def routing-set (router/destruct-definition our-routes))
  (start-server 8080 our-routes)
  )

