(ns helmsman.example-site
  (:require 
    [helmsman :as h]
    [helmsman.router :as router]
    [helmsman.navigation :as nav]
    [helmsman.uri :as uri]))

(defn basic-html-doc
  "Creates a basic template for an HTML page."
  [body]
  (str "<!doctype html><html><body>"
       body "</body></html>"))

(defn home-page-body
  "Creates the basic body for a basic navigational page."
  [request]
  (let [add-uri (nav/id->uri request ::add
                             :one 4 :two 8)
        sub-uri (nav/id->uri request ::subtract
                             :one 3 :two 13)
        mult-uri (nav/id->uri request ::multiply
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
  (home-page-body
    request))

(defn punt
  "Converts a string to an integer, basic wrapper."
  [n] (Integer/parseInt n))

(defn return-home
  "Creates an a tag with a link back to the home page."
  [request]
  (str "<a href=\""
       (nav/id->uri request ::home)
       "\">Go Home</a>"))

(h/defhandler add-page
  [one two :as request]
  (str "Result: " (+ (punt one) (punt two))
       "<br />"
       (home-page-body request)
       (return-home request)))

(h/defhandler subtract-page
  [one two :as request]
  (str "Result: " (- (punt one) (punt two))
       "<br />"
       (home-page-body request)
  (return-home request)))
  
(h/defhandler multiply-page
  [one two :as request]
  (str "Result: " (* (punt one) (punt two))
       "<br />"
       (home-page-body request)
       (return-home request)))

(h/defhandler debug-page
  [:as request]
  (prn-str request))

(def our-routes
  [[constantly :foobar]
   ^{:id ::home}
   [:get "/" home-page]
   [:context "math"
    [constantly :insane]
    ^{:id ::add}
    [:get "add/:one/:two" add-page]
    ^{:id ::subtract}
    [:get "subtract/:one/:two" subtract-page]
    [constantly :annoying]
    ^{:id ::multiply}
    [:get "multiply/:one/:two" multiply-page]]
   [constantly :a-tool]
   ^{:id ::debug}
   [:get "/debugging" debug-page]
   ])

(comment
  (def application (h/compile-routes our-routes))
  (def meta-data (h/compile-meta our-routes)))

