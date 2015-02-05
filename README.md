# Helmsman

## Brief background
Helmsman is a routing library for Ring. At first, Helmsman was built on top of
Compojure, utilizing all the non-macro underworkings for creating routes.
Quickly did I realize that between doing that and using zippers for traversing
a definition was brutal to manage or even understand. Since the project
Helmsman was initially part of another project, Galleon, which died off, it
slowly accumulated some dust until I was inspired and motivated to finish what
I started in my own time as it's something I feel rather strongly about. Like
in the early days with Galleon, I once again needed a *half decent* routing
library for a different project, Informer, that still supported all of your
typical Ring middlewares (as much as I personally disagree with them).

## The problems
There were some very specific things that I wanted to solve in Helmsman that
I felt that Compojure and many other routing libraries did not solve.

### Routes as data
As Clojure developers, we love data. It only makes sense for our routes to be
data as well. Compojure builds routes up by using macros. To add insult to
injury, once the routes have been "composed" together you have a handler
without anything special and the real trick comes when you say "How do I link
to another route without redefining all of our routes?" The simple fact is that
with Compojure, you can't, so you're stuck with the options of hacking up
Compojure to do what you want (like I initially did, which was insane) or just
biting the bullet and re-inventing the wheel.

Needless to say, I tried using the wheel I already had and it was a pretty
cruddy wheel, so I set off to build a better one.

Helmsman uses strictly data to create a Ring handler. No messing with macros,
you just get down and dirty with vectors without any insanity to build them up.
As a result, the same data used to make the Ring handler can be used for
anything else in your application inside or outside of a Ring request.

### Routing before processing
One thing that blows my mind more than anything else is how when using
Compojure, it needs to load up the world of Middleware before routes are even
checked to see if the request path matches the route path. As a result, a bunch
of Middleware can run even if no route is encountered. For a web server handling
many ajax requests at once, that could really hurt you in terms of performance
for simply figuring out that you got a 404.

Since Helmsman compiles a structure of what all the routes look like ahead of
time, we store all of the middleware required for any given route along with
any information Helmsman has. As a result, we can check to see if the request
and route paths match before executing any middleware. In fact Helmsman stores
a fn that already has the route fn wrapped in all the middleware before the
web server is even brought up. So after the paths get checked, Helmsman simply
calls the fn without any extra processing, minimizing the amount of work done at
the time of the request.

### Alternate routing methods
As it stands, Compojure and other routing libraries route in one way and one way
only. Well, as it stands right now, so does Helmsman but that's not intended to
be a long term goal. Code exists that could accelerate routing time but is on
hold for the time being.

### Leveraging your routes
Compojure only puts your routes together, it doesn't actually let you leverage
them. While strict fn-based routing might be simple and okay, it doesn't provide
any tools for navigation or using the routes themselves to do things. As a
result, you're being routed without knowing anything about the routing itself.

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

## The biggest limitation/perk of Helmsman
The idea of a "wild card" character in Helmsman dies. In order to be able to
handle pathing effectively, we can't support something as abiguous as a wild
card and be able to effectively generate URIs to and from those locations. You
can still use named parameters in URIs such as ```"/this/:item/set-id/:id"```
but you can't do something like ```"/this/*/:item/set-id/:id```. The upside to
this is that it enforces a stricter URI convention that encourages specfic
URIs over ambiguous ones.

## Dependencies?!
The latest and greatest version of Helmsman can be used with the following
dependency string for a Leiningen project.
```clojure
[org.vlacs/helmsman "1.0.0-SNAPSHOT"]
```

It can be aquired on Clojars once this version has been released.

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
