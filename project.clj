(defproject clj-rest-svc "1.0.0-SNAPSHOT"
  :description "Simple REST hypermedia service"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		;[compojure "0.4.0"]
		 [compojure "0.6.0-RC1"]
		 [ring/ring-jetty-adapter "0.3.5"]]
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :main clj-rest-svc.core)


