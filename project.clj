(defproject org.vlacs/helmsman "0.2.3"
  :description "This is a library for describing the structure of a web appliction.
               This way, we have uris and data associated directly with routes.
               This is nice for generating navigation or applying middleware to
               routes are a particular level."
  :url "https://github.com/vlacs/helmsman"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.2.2"]
                 [compojure "1.1.6" :exclodes [ring/ring-core]]
                 [com.taoensso/timbre "3.1.6"]]
  :plugins [[lein-cloverage "1.0.2"]]
  :pedantic? :warn
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.nrepl "0.2.3"]
                                  [org.clojure/tools.namespace "0.2.4"]
                                  [ring-mock "0.1.5"]]}})

