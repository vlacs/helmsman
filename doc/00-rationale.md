# Helmsman: The rationale for its inception
## Brief background
Helmsman is a routing library for Ring. At first, Helmsman was built on top of
Compojure, utilizing all the non-macro underworkings for creating routes.
Quickly did I realize that between doing that and using zippers for traversing
a definition was brutal to manage or even understand. Since the project
Helmsman was initially part of another project, Galleon, which died off, it
slowly accumulated some dust until I was inspired and motivated to finish what
I started in my own time as it's something I feel rather strongly about. Like
in the early days with Galleon, I once again needed a *half decent* routing
library for a different project that still supported all of your typical Ring
middlewares (as much as I personally disagree with them and how they work).

## The problems
There are some pretty big systemic issues with routing libraries in general, not
even just with some of the more commonly used ones like Compojure, Bidi, and
Liberator which provide functionality for doing some of what Helmsman is going
to do, but none of them fully replicate it (where Bidi actually gets close).

### Routes as data
This is where Compojure and Liberator get a failure in my book. Compojure
utilizes wrapping handlers inside eachother to make things happen to a request
over time. Once a request comes in, it kind of goes through a forest of
craziness until you emerge in the handler and if something breaks, you have
a call stack a mile long (Liberator is the same way in that respect.) Bidi
however, does represent routes as data, but it doesn't represent the entire set
of routes as data in a way that I'm confortable with. There are still too many
reminents of Compojure in my personal opinion.

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

### Leveraging the data contained in your routes
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

### URIs; Paths, paths, paths.
Due to Helmsman's concept of navigation via URI paths is a cornerstone of the
project, the idea of a "wild card" character in Helmsman dies. In order to be
able to handle pathing effectively, we can't support something as abiguous as
a wild card and be able to effectively generate URIs to and from those
locations. You can still use named parameters in URIs such as
```"/this/:item/set-id/:id"``` but you can't do something like
```"/this/*/:item/set-id/:id```. The upside to this is that it enforces a
stricter URI convention that encourages specfic URIs over ambiguous ones.

[1: Defining routes](https://github.com/vlacs/helmsman/blob/dev/doc/01-defining-routes.md)

