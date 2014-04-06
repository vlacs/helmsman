# Helmsman [![Build Status](https://travis-ci.org/vlacs/helmsman.png?branch=master)](https://travis-ci.org/vlacs/helmsman)
===============
<a title="By Steering_wheel_ship_1.png: Lidingo derivative work: Arnaud Ramey (Steering_wheel_ship_1.png) [CC-BY-SA-3.0 (http://creativecommons.org/licenses/by-sa/3.0) or GFDL (http://www.gnu.org/copyleft/fdl.html)], via Wikimedia Commons" href="http://commons.wikimedia.org/wiki/File%3ASteering_wheel_ship.svg"><img width="128" alt="Steering wheel ship" src="http://upload.wikimedia.org/wikipedia/commons/thumb/2/2b/Steering_wheel_ship.svg/128px-Steering_wheel_ship.svg.png"/></a>

Helmsman steers your app by letting you compose Compojure routes and the structure of your application at the same time.

Here is the latest leiningen dependancy string for your project.clj file:

```clojure
[org.vlacs/helmsman "0.1.8"]
```

TODO: More description!

## Usage

A website in Helmsman is designed to process a series of nested vectors. Each vector
is a compojure component that is getting composed together. The goal is to make
writing routes and applying middleware to be one in the same thing in addition to
having a system that allows you to tie meta-data (real Clojure meta data,) to
any particular portion of the application, but not losing it when the routes are
composed together and eventually executed. In particular, knowing about uris
associated with different routes to be able to create reliable uri for links,
absolute or relative.

Every route will always be created like this:
```clojure
[:http-method "/some/compojure/uri" some-handler-fn ... ]
```

Valid http methods are the same as compojure and are defined in Helmsman as:
```clojure
#{:get :head :post
  :put :delete :trace
  :options :connect :patch}
```

Nested routes use the uri of the previous routes and contexts by prepending it
to their own. That way routes are built up and uri segments aren't repeated when
a definition is created. It also allows applications to define how their segment
of a web application will look without knowing how the layers above it will.

Much like compojure, a context can be applied by using the :context keyword. It
behaves exactly like a route, except it doesn't have a handler and is written
like a route but with two arguments, like this:
```clojure
[:context "/some/path"
  [:get "/testing" some-handler]
  [:get "/testing2" some-other-handler]]
```

Which would result in two routes: /some/path/testing and /some/path/testing1.

Any middleware that gets added as if the handler will be threaded (->) through the respective
function call and can be created like this:
```clojure
[any-middleware-fn arg-1 arg-2 ... arg-n]
```

TODO: Make an example that ties all of this together.
```clojure
(helmsman/compile-routes [[:any "/" (constantly "Hello world!")]])
```

Would be the same as
```clojure
(compojure/routes (compojure/ANY "/" (constantly "Hello world!")))
```

Helmsman also supports serving up static files. We handle this exactly the same
way that compojure does. For example, these two statements are analogous when
using helmsman:
```clojure
(compojure.route/resources "/")
[:resources "/"]
```

## License

Copyright VLACS© 2014

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
