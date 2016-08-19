(defproject org.vlacs/helmsman "1.0.0"
  :description "This is a library for describing the structure of a web appliction.
               This way, we have uris and data associated directly with routes.
               This is nice for generating navigation or applying middleware to
               routes are a particular level."
  :url "https://github.com/vlacs/helmsman"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring "1.4.0"]
                 ;;; Compojure is about to get ditched once the dev example site
                 ;;; is updated to not use compojure handlers.
                 [compojure "1.1.6" :exclusions [ring/ring-core]]
                 [com.taoensso/timbre "4.1.4" :exclusions [org.clojure/tools.reader]]]
  :pedantic? :warn
  :source-paths ["src" "src/main/clojure"]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.nrepl "0.2.7"]
                                  [ring/ring-mock "0.2.0"]
                                  [org.clojure/test.check "0.7.0"]]}})

