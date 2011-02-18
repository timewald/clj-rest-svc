(ns clj-rest-svc.core
  (:gen-class)
  (:use compojure.core
	ring.adapter.jetty)
  (:require [clojure (string :as string) (set :as set)]
	    [compojure.route :as route]))

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

(defn clj-resp [f] {:status 200
		    :headers {"Content-Type" "x-application/clojure"}
		    :body (binding [*print-meta* true] (prn-str f))})

; Service implementation
; TBD: split this into separate namespaces

; model (sort of)
(def *assets* (atom { 1 {:id 1 :title "asset 1" :desc "interesting 1" }
		      2 {:id 2 :title "asset 2" :desc "interesting 2" }}))

; views
(defn assets-index-view [assets]
  (clj-resp
   (vec
    (map
     (fn [[k v]]
       (assoc (dissoc v :desc)
	 :details (gen-link :get-assets-id {:id (v :id)})))
     assets))))

(defn asset-show-view [asset] (clj-resp asset))

; controllers

(defn assets-index [] (assets-index-view @*assets*))

(defn asset-show [id] (asset-show-view (@*assets* id)))

;(defn asset-create [asset]
;  (swap! *assets* assoc (asset :id) asset)
;  {:status 201 :headers {"Location" "absolute URL goes here"} :body nil})

(defroutes-and-generators example
  (GET "/assets" [] (assets-index))
  (GET "/assets/:id" [id] (asset-show (Integer/parseInt id)))
  (POST "/assets" {body :body} body))


