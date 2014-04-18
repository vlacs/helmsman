# Helmsman [![Build Status](https://travis-ci.org/vlacs/helmsman.png?branch=master)](https://travis-ci.org/vlacs/helmsman)
===============
<a title="By Steering_wheel_ship_1.png: Lidingo derivative work: Arnaud Ramey (Steering_wheel_ship_1.png) [CC-BY-SA-3.0 (http://creativecommons.org/licenses/by-sa/3.0) or GFDL (http://www.gnu.org/copyleft/fdl.html)], via Wikimedia Commons" href="http://commons.wikimedia.org/wiki/File%3ASteering_wheel_ship.svg"><img width="128" alt="Steering wheel ship" src="http://upload.wikimedia.org/wikipedia/commons/thumb/2/2b/Steering_wheel_ship.svg/128px-Steering_wheel_ship.svg.png"/></a>

Helmsman steers your app by letting you compose Compojure routes and the structure of your
application at the same time by holding on to data you use to create routes in
the first place and leveraging it.

Here is the latest leiningen dependancy string for your project.clj file:

```clojure
[org.vlacs/helmsman "0.2.0"]
```

## What does Helmsman do?

A website using Helmsman is designed to process a series of nested vectors. Each vector
is a Compojure-like component that is getting composed together. The goal is to make
writing routes and applying middleware to be one in the same thing in addition to
having a system that allows you to tie meta-data (real Clojure meta data,) to
any particular portion of the application, but not losing it when the routes are
composed together and eventually executed. In particular, knowing about URIs
associated with different routes will enable you to be able to create reliable uri
for links; This includes relative URIs (which are prefered) and absolute URIs.

# Route and handler creation

## Routes
Helmsman routes are very similar to compojures in design. The difference is that
we try to leverage data structures that we can work with whenever possible. As
a result, every route will be created like this:
```clojure
[:http-method "/some/compojure/uri" some-handler-fn ... ]
```

Valid http methods are the same as compojure and are defined in Helmsman as
the following set as defined in Helmsman's code.
```clojure
#{:get :head :post
  :put :delete :trace
  :options :connect :patch}
```

## Nested routes
Nested routes use the uri of the previous routes and contexts by prepending it
to their own. That way routes are built up, uri segments aren't repeated when
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

## Where are my request bindings?!
Matt Oquist brought up a really good point with me the other day that in Compojure,
routes automagically let you destructure the request when you define the handler. In
response I wrote a macro that uses Compojure's own let-request macro that does
just that. To use it, define your handlers like so:
```clojure
(helmsman/defhandler
  [:as request]
  (do-something-with request))
```

## Files and resources

There are also other route-like keywords you can use that create particular
kinds of routes. For example, you can use resources in the very same way you can
in Compojure.
```clojure
;;; You can use resources based on your class path.
[:resources "/public" {:root "app-files/static"}]
;;; Or files based on the systems root.
[:files "/some-dir" {:root "/var/some-app/appdata"}]
```

## Middleware
Middleware in Helmsman is the same as though you were calling it within the
context of the thread (->) macro. It also applies the middleware to all the
routes that are at in this level or within deeper levels with respect to the
location the middleware is being applied.

For example, using middleware looks like this:
```clojure
[any-middleware-fn arg-1 arg-2 ... arg-n]
```

So if you were to do something like this, the second route ```/bar``` would have the
middleware applied to it, where the first and last, ```/``` and ```/foo```
respectively  will not.
```clojure
[:get "/" (constantly "Hello world. :)")
  [:get "/bar" predefined-handler]
  [some-middleware arg-1 arg-2 arg-n]
  [:get "/foo" another-handler]]
```

## Getting your handler
Well, you need something to give Jetty, Immutant, or whatever you use for a web
server. There is a single function that handles this for you and it's called
```helmsman/compile-routes``` and it will return a single handler function like
Compojure does, which you can just hand to your web server. Helmsman does apply
its own middleware to your application in order to add the map on the
```:helmsman``` key of the request map. It contains meta data and URI
information which will be touched upon next.

# Leveraging Helmsman beyond routes
Helmsman is nifty in the sense that once you define your routes, meta-data is
accessible to everything and on top of that we parse the uri from the request so
given where we are and the routes we can go, we can relatively easily create
URIs to anything we're aware of if there is meta-data that uniquely idenfies
a route.

## Getting information from meta-data
Meta data is stored by default, in only one place. The ring request when
requests come in to be processed. Optionally, you can compile the meta-data
before compiling the routes and define them somewhere, but there are cases were
it is important to have this data accessible from the request.

If the meta-data is in the request, you can get it by calling:
```clojure
(get-in request [:helmsman :all-meta])
```

With the meta-data you can either use predicates to find a single item or to
limit the size of the set. Both of these rely on the fn
```helmsman.navigation/meta-filter``` which really is just a wrapper for
```clojure.set/select``` . ```meta-filter``` returns a new set. You can get
a single item based on a predicate by calling
```helmsman.navigation/meta-get-unique``` which takes the same parameters as
meta-filter, the meta-data set and the predicate. What you want out of the map
completely depends on the meta data you attach to your routes. So if you had an
:id field in some meta-data items, you could do the following to get one or
many:
```clojure
(helmsman.navigation/meta-filter
  meta-data
  (helmsman.naviagtion.preds/with :id "some-id"))
```

For that particular example :id is a special keyword so we have a function
dedicated to getting a single item with a value on that key:
```clojure
(helmsman.navigation/meta-with-id meta-data "id-name")
```

Note: You can combine predicates in clojure by using
```clojure.core/every-pred``` to merge your predicates into a single one and all
of helmsman's predefined predicates for navigation will be in
```helmsman.navigation.preds``` .

## Relative URIs made simple
Given two distinct URI under the same hostname, we can reliable determine how
far up the tree we need to go before we start going back down it on the same
point as the destination URI. Helmsman can do that, you just need to know where
you are and where you're going, if you don't have either of those, you're lost.

Internally Helmsman generates URIs as a vector with each item being a URI
segment. Internally helmsman uses a two-level vector to handle special cases for
things like nesting routes and contexts, but you'll never encounter them. All
URIs that come out of helmsman (before it turns them into a string,) are
vectors.

For example, you have two addresses on the same host;
```http://www.somerandomhost.com/working/on/the/railroad``` and
```http://www.somerandomhost.com/working/with/people``` . The first contains the
URI ```/working/on/the/railroad``` which Helmsman turns into
```["working" "on" "the" "railroad"]``` . Same thing with the second address. It
has the URI ```/working/with/people``` which Helmsman turns into 
```["working" "with" "people"``` .

Using these uri paths (vector representation of a URI) we can very easily make
a relative URI either in helmsman format or in string format.
```clojure
(helmsman.uri/relative-uri ["working" "on" "the" "railroad"]
                           ["working" "with" "people"])
;;; Returns [".." ".." "with" "people"]
(helmsman.uri/assemble [".." ".." "with" "people"])
;;; Returns the string representation "../../with/people"
```

## License

Copyright VLACS© 2014

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
