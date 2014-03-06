# Helmsman

A Clojure library design to let you compose Compojure routes and define the
structure of your application at the same time as opposed to strictly building
up just the routes alone.

TODO: More description!

## Usage

A website in Helmsman is designed to process a series or nested vectors. Each vector
is a compojure component that is getting composed together. The goal is to make
writing routes and applying middleware to be one in the same thing in addition to
having a system that allows you to tie meta-data (real Clojure meta data,) to
any particular portion of the application, but not losing it when the routes are
composed together.


You can define a simple website like so (with some meta-data).


```clojure
[^{:name "Home page" :main-menu true}
 [:any "/" (constantly "Hello world!")]
 ^{:name "The foobar page" :main-menu true}}
 [:any "/foobar" (constantly "A different page!")
  [:any "/baz/:something" (constantly "Another handler!")]]]
```


This creates three different routes: ```/``` ```/foobar``` and ``` /foobar/baz/:something ```
where the ```/``` and ```/foobar``` routes are being marked (but not altered) to
reflect data about these routes.


It's also worth noting that all routes have 3 (and only 3) attributes or
arguments if you will. ```clojure
[:http-method "/some/compojure/uri" some-handler-fn]```


TODO: Add usage for middleware and contexts.

## License

Copyright VLACS© 2014

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
