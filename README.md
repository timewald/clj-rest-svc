# clj-rest-svc

A Hypermedia API written in Clojure.

## Usage

Once you have the server running with `lein run`, you can use `curl` to view responses from the server:

```
$ curl http://localhost:8080/assets
(
	{
		:details {:method "get", :href "/assets/1"},
	 	:id 1, 
	 	:title "asset 1"
	}
 	{	
 		:details {:method "get", :href "/assets/2"},
 		:id 2,
 		:title "asset 2"
 	}
)
```
Note that the output will not be pretty-printed as you see above but will show up as one long line.

## Installation

Clone the repository:

```
git clone git://github.com/timewald/clj-rest-svc.git
```
Then navigate to the `clj-rest-svc` directory:

```
cd clj-rest-svc
```
Finally, run the command `lein run`. This assumes you have [Leiningen](http://leiningen.org) installed.

## License

Copyright (C) 2010 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
