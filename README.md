# Helmsman

## Brief background
Helmsman is a routing library for Ring. At first, Helmsman was built on top of
Compojure, utilizing all the non-macro underworkings for creating routes.
Quickly did I realize that between doing that and using zippers for traversing
a definition was brutal. Since the project Helmsman was initially part of
a project that died of, it slowly accumulated some dust until I was inspired
and motivated to finish what I started as once again, I needed a half decent
routing library for a different project that still supported all of your typical
Ring middlewares.

## The problems
There were some very specific things that I wanted to solve in Helmsman that
I felt that Compojure did not solve.

#### Routes as data
Compojure builds routes up by using macros. To add insult to injury, once the
routes have been "composed" together you have a handler without anything
special and the real trick comes when you say "How do I link to another route
without redefining all of our routes?" The simple fact is that with Compojure,
you can't, so you natural either have nasty code or a shoddy workaround.

Helmsman uses strictly data to create a Ring handler. No messing with macros,
just get down and dirty with vectors. As a result, the same data used to make
the Ring handler can be used to generate relative URLs based on routes that
actually exist in your application.

### Routing before processing
One thing that blows my mind more than anything else is how when using
Compojure, you need to load up the world of Middleware before routes are even
checked to see if the request path matches the route path. As a result, a bunch
of Middleware can run even if no route is encountered.

Since Helmsman compiles a structure of what all the routes look like, we store
all of the middleware required for any given route. As a result, we can check to
see if the request and route paths match before executing the middleware. In
fact Helmsman stores a fn that already has the route fn wrapped in all the
middleware after the route gets checked, so processing at the time of the
request is kept to a minimum.

### Alternate routing methods
As it stands, Compojure and other routing libraries route in one way and one way
only. Well, as it stands right now, so does Helmsman but that's not intended to
be a long term goal. Code exists that could accelerate routing time but is on
hold for the time being.

### Leveraging your routes
Compojure only puts your routes together, it doesn't actually let you leverage
it. While strict fn-based routing might be simple and okay, it doesn't provide
any tools for navigation. As a result, you're being routed without knowing
anything about the routing itself.

Helmsman allows for Clojure meta data to be attached to individual routes that
will be included in the routing set that Helmsman generates. The biggest need
was to be able to identify a route using a keyword. So you can write a route
like this to attach data to it:
```clojure
(def my-routes [^{:id :home}[:get "/" {:status 200 :body "Home page."}]])
```

Helmsman breaks down paths for every route when the routing set is compiled and
Helmsman offers facilities via the ```helmsman.uri``` namespace to create
relative URIs as well as the ```helmsman.navigation``` namespace to "find" other
routes in your application as opposed to redefining them. Also, once a request
has been routed, Helmsman pulls aside the current route and puts it in the
request map at ```[:helmsman :current-route]``` for easy usage. Helmsman puts
a whole bunch of useful data derived from the application definition in the
```:helmsman``` key of the request map to make a developer's life a little
easier.

## project.clj
The latest and greatest version of Helmsman can be used with the following
dependency string for a Leiningen project.
```clojure
[org.vlacs/helmsman "1.0.0-SNAPSHOT"]
```

It can be aquired on Clojars once this version has been released.

## What does Helmsman do?
Simply put, Helmsman at its heart lets you turn data into a Ring handler. It
does many of the things other routing libraries like Compojure do, but Helmsman
is built up using strictly a data structure.

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
    [:get "/:user-id {:status 200 :body "Some user page."]]])
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
