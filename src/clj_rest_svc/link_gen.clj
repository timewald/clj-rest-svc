(ns clj-rest-svc.link-gen
  (:use compojure.core)
  (:require [clojure (string :as string) (set :as set)]))

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
  (keyword (str method "-" (string/join "-" cleaned-path)))))

(defn replace-name [name param-map]
  (if (is-param-name? name)
    (param-map (keyword (apply str (rest name)))) ; yuck
    name))

; ??? need to add query string for extra params
(defn generator-function [path]
  (fn [params]
    (let [path-params (set (filter is-param-name? path))
          query-params (set/difference (set params) path-params)
	  replaced-path (map #(replace-name % params) path)]
      (string/join "/" replaced-path))))

(defn generator-map-entry [route]
  (let [method (string/lower-case (str (first route)))
	path (string/split (nth route 1) #"/")]
    {(generator-name method path) (generator-function path)}))

(defn defgenerators [args]
  (def *generators* (apply merge (map generator-map-entry args))))

(defn gen-url [path-name param-map]
  ((*generators* path-name) param-map))

(defn gen-link [path-name param-map]
  (let [href (gen-url path-name param-map)
	method (apply str (rest (first (string/split (str path-name) #"-"))))
	link {:link true}]
    ^link {:method method :href (gen-url path-name param-map)}))

(defmacro defroutes-and-generators [name & args]
  `(do (defgenerators '~args)
       (defroutes ~name ~@args)))


