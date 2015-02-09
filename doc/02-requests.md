# Helmsman documentation
# 2: Helmsman and the Ring request

## The handler
Helmsman uses any ordinary Ring handler for each route. It is a single arity
function that takes in a prepared Ring request map. If you are not familiar with
Ring request maps, you can find some documentation on it
[here](https://github.com/ring-clojure/ring/wiki/Concepts#requests). Helmsman
simply adds another item to the Ring request with the key ```:helmsman``` and
the value is a map. This map contains all of the information derived from the
definition and the current route that the request invoked. The Helmsman section
of the Ring request will have a map with the following keys:

- ```:request-path``` is the Helmsman path derived from the Ring request that
  was used to route the request.
- ```:request-signature``` is a vector of characters that represents a simple
  non-unique identifier for the current request.
- ```:routing-set``` is a Clojure set which contains the definitions of all the
  routes in the definition that was used and all of the prepared data Helmsman
  makes for any given route.
- ```:current-route``` is a Clojure map that represents a single routes, the
  route the current request resolved to. This route item is from the
  ```:routing-set```.
- ```:request-path-params``` is a Clojure map the resolves path parameters in
  a definition to their actual values from a request. No parameters will
  result in an empty map.

## The prepared Helmsman route
These are the maps that exist inside the ```:routing-set``` item in the Helmsman
portion of the Ring request.

TODO: Finish this.
