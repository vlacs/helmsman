# Helmsman Documentation
# 1: Defining routes

Definting routes is pretty simple. It should simply consist of nested vectors.
Nothing more, nothing less. There are a couple standard forms for building up
routes. For the sake of simplicity, I will be assuming the following variables
are defined.

```clojure
(def default-response {:status 200 :body "Hello world!"})
```

## A single route
A single route will take on a form like this:
```clojure
[:get "/some/uri/path" default-response]
```

The first positional argument for routes is a set of keywords that directly
represent all valid HTTP verbs and is equitable to the following set:
```clojure
#{:get :head :post :put :delete :trace :options :connect :patch}
```

There is also a special keyword ```:any``` that matches all HTTP keywords. The
possibility of supplying a set of http verbs is not yet supported but may find
its way into a future release if there is a demand for it.

The second parameter is a string that describes a URI for any given route much
like any routing library would. For example, an incoming HTTP GET request for
```http://localhost/this/is/a/test``` would match a route such this:

```clojure
[:get "/this/is/a/test" default-response]
```

The last argument is simply the Ring handler associated with the route. It
should be a ```fn``` with an arity of one; the prepared Ring request.

## Multiple routes
Now, one route is boring. If we only ever needed one route, there would be no
need for routing libraries. So there are a lot of nifty things that can be done
with routes.

The most basic of things would be having multiple routes side by side:
```clojure
[[:get "/path/one" default-response]
 [:get "path/two" default-response]
 [:get "path/three/" default-response]]
```

There is one big thing to note on this particular example. Any slashes at the
beginning or end of paths are stripped out. This is true not only for the paths
we define but for the incoming requests as well.

## Nesting routes
That last example was kind of boring as well though and to make matters worse,
a pattern has asrisen, but that is okay, because Helmsman can handle that. Just
like Compojure, Helmsman can use a special ```:context``` verb to unite routes
under a common banner:
```clojure
[[:context "path"
  [:get "one" default-response]
  [:get "two" default-response]
  [:get "three" default-response]]]
```

Building routes up in this manner is one of the building blocks to Helmsman.
This composibility is not limit to just ```:context``` stanzas either. Any items
following the first 3 route arguments will be considered in the context of the
first route. For example, if I wanted to handle a ```path``` route without any
of the numbers, the example would look like this:
```clojure
[[:get "path" default-response
  [:get "one" default-response]
  [:get "two" default-response]
  [:get "three" default-response]]]
```

## Path parameters
Routes are also allowed to have path-level parameters like in Compojure and
other routing libraries by using keyword syntax in a route path. We can merge
all of these routes into one with the following example:
```clojure
[[:get "path" default-response
  [:get ":var-name" default-response]]]
```

In this case, one, two and three (along with any other value) would be sent to
that handler with a path parameter ```:var-name``` with whatever value is in the
path of the request.

It is important to note that all paths are delimited by slashes and that
Helmsman has an entire namespace dedicated to handling these which will be
touched on later in the documentation. Needless to say, the following path would
be valid:
```clojure
[[:get "this/:path/will/:be/:valid" default-response]]
```

A path variable is the contents of everything between two slashes or the
beginning and/or end of the path. How to use these is explained later.

## Middleware
Ahh, middleware. There is no escaping it. It will always be around to haunt us,
but it is a neccessary evil. As a result, Helmsman supports all of your
traditional Ring middlewares. How this all works on under the hood will be
touched on elsewhere.

For a moment, lets assume we are hungry for cookies, so we have required the
Ring cookie namespace ```ring.middleware.cookies``` locally. Using one of the
examples defined in the past, we can apply cookies to all of our routes, simply
by adding it as the very first item in the routes.
```clojure
[[ring.middleware.cookies/wrap-cookies]
 [:get "path" default-response
  [:get "one" default-response]
  [:get "two" default-response]
  [:get "three" default-response]]]
```

Simple enough, right? We basically state that we want to wrap all of the
following routes in the middleware ```[ring.middleware.cookies/wrap-cookies]```.
This is the same thing as having a ```ring-handler``` and doing this:
```clojure
(-> ring-handler
  ring.middleware.cookies/wrap-cookies)
```

Well, maybe our top-level "path" route does not need cookies, but the remaining
do. That is simple too, we just shift the Middleware up a level.
```clojure
[[:get "path" default-response
  [ring.middleware.cookies/wrap-cookies]
  [:get "one" default-response]
  [:get "two" default-response]
  [:get "three" default-response]]]
```

Now the Middleware gets applied to only the routes contained in the ```path```
context but not the ```path``` route itself. The same behavior can be applied by
moving the middleware down one more route.
```clojure
[[:get "path" default-response
  [:get "one" default-response]
  [ring.middleware.cookies/wrap-cookies]
  [:get "two" default-response]
  [:get "three" default-response]]
 [:get "something-extra" default-response]]
```

The follow example incudes cookies on the last two routes, but not the first
two. The middleware is also contained to the current context and is only applied
to anything after or below it, so the ```something-extra``` route I added would
never be touched by cookies.

## Files and resources
Working on it. This is not currently supported as of ```1.0.0-alpha1```.

[2: Helmsman and the Ring request](https://github.com/vlacs/helmsman/blob/dev/02-requests.md)

