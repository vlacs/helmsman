# Helmsman

A Clojure library design to let you compose Compojure routes and define the
structure of your application at the same time as opposed to strictly building
up just the routes alone.

## Usage

You define a simple website like so!


```clojure
[[:any "/" (constantly "Hello world!")]
 [:any "/foobar" (constantly "A different page!")
  [:any "/baz/:something" (constantly "Another handler!")]]]
```


This creates three different routes: ```/``` ```/foobar``` and
```/foobar/baz/:something```


TODO: Add more usage.

## License

Copyright VLACS© 2014

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
