# Helmsman

## Why was this library created?
You can find the [rationale document
here](https://github.com/vlacs/helmsman/blob/f/raw-ring/doc/rationale.md).

## Dependencies?!
The latest and greatest version of Helmsman can be used with the following
dependency string for a Leiningen project once 1.0 is uploaded to Clojars.
```clojure
[org.vlacs/helmsman "1.0.0-alpha1"]
```

## Using Helmsman to do things

Here are some basic examples of how routes can be written. I will create some
better tutorials once the library has been polished up.

This is a hello world route.
```clojure
(def my-routes
  [[:get "/" {:status 200 :body "Hello world"}]])
```

You can have may routes at the same level, or routes in nested levels.
```clojure
(def my-routes
  [[:get "/" {:status 200 :body "Hello world"}]
   [:get "/page-2" {:status 200 :body "Another page!"}]
   [:get "/user" {:status 404 :body "Nothing here."}
    [:get "/:user-id" {:status 200 :body "Some user page."]]])
```

You can nest routes without defining a route itself by using a ```:context```
and we can use any HTTP method supported by Ring and the web server by using
keywords such as ```:post```, ```:options```, ```:get```, etc.
```clojure
(def my-routes
  [[:context "/user/:user-id"
    [:get "/about" {:status 200 :body "Something."}]
    [:post "/edit" {:status 200 :body "Edit something."}]]])
```

# TODO: Finish the new and improved README
### Also have someone proof read it.

## License

### Copyright and credits
 - VLACS© <jdoane@vlacs.org> 2014
 - Jon Doane <jrdoane@gmail.com> 2014-2015

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
