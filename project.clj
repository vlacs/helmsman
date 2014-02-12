(defproject plumber "0.0.1-SNAPSHOT"
  :description "This is a library for describing the structure of a web appliction.
               This way, we have uris and data associated directly with routes.
               This is nice for generating navigation or applying middleware to
               routes are a particular level."
  :url "http://thiswillbeongithubsoon.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring "1.2.1"] 
                 ]
  :main ^{:skip-aot true} plumber.core)
