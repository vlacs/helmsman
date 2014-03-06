(defproject helmsman "0.1.0"
  :description "This is a library for describing the structure of a web appliction.
               This way, we have uris and data associated directly with routes.
               This is nice for generating navigation or applying middleware to
               routes are a particular level."
  :url "https://github.com/vlacs/helmsman"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring "1.2.1"]
                 [com.taoensso/timbre "3.0.1"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.nrepl "0.2.3"]
                                  [org.clojure/tools.namespace "0.2.4"]
                                  [ring-mock "0.1.5"]]}})
