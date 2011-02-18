(ns clj-rest-svc.core
  (:gen-class)
  (:use compojure.core
	ring.adapter.jetty)
  (:require [compojure.route :as route]))

; Service bootstrap plumbing for interactive or stand-alone processing

(declare example)

(def run-server #(run-jetty #'example {:port 8080}))

(defn- dev-server []
  (doto (Thread. run-server) .start))

(defn -main [& args] (run-server))

;;; URL generator functions

(defn is-param-name? [p]
  (= \: (first p)))

(defn clean-param-name [p]
  (if (is-param-name? p)
    (apply str (rest p))
    p))

(defn generator-name [method path]
  (let [filtered-path (filter #(seq %) path)
	cleaned-path (map clean-param-name filtered-path)]
  (keyword (str method "-" (clojure.string/join "-" cleaned-path)))))

(defn replace-name [name param-map]
  (if (is-param-name? name)
    (param-map (keyword (apply str (rest name)))) ; yuck
    name))

; ??? need to add query string for extra params
(defn generator-function [path]
  (fn [params]
    (let [path-params (set (filter is-param-name? path))
          query-params (clojure.set/difference (set params) path-params)
	  replaced-path (map #(replace-name % params) path)]
      (clojure.string/join "/" replaced-path))))

(defn generator-map-entry [route]
  (let [method (clojure.string/lower-case (str (first route)))
	path (clojure.string/split (nth route 1) #"/")]
    {(generator-name method path) (generator-function path)}))

(defn defgenerators [args]
  (def *generators* (apply merge (map generator-map-entry args))))

(defn generate [path-name param-map]
  ((*generators* path-name) param-map))

(defmacro defroutes-and-generators [name & args]
  `(do (defgenerators '~args)
       (defroutes ~name ~@args)))

(def *assets* (atom #{})

(defn assets [] "<h2>assets!</h2>")

(defn asset [id] (str "<h2>asset " id "</h2>"))

(defroutes-and-generators example
  (GET "/assets" [] (assets))
  (GET "/assets/:id" [id] (asset id)))

;  (POST "/assets" [] "<h1>Created asset, should redirect"))

