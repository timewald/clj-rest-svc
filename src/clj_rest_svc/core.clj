(ns clj-rest-svc.core
  (:use [ring.adapter.jetty :only [run-jetty]]
	[ring.middleware.params :only (wrap-params)]        
	[ring.middleware.keyword-params :only [wrap-keyword-params]]
        [compojure.core :only [GET POST]]
	[clj-rest-svc.link-gen :only [gen-link defroutes-and-generators]])
  (:require [clojure (string :as string) (set :as set)]
	    [compojure.route :as route]
	    [clj-rest-svc.logging :as logging]))

; Service bootstrap plumbing for interactive or stand-alone processing

(def clj-mime-type "x-application/clojure")

(defn clj-resp
  ([form] (clj-resp 200 form))
  ([status form] (clj-resp status {"Content-Type" clj-mime-type} form))
  ([status headers form]
     {:status status
      :headers headers
      :body (prn-str form)}))

(defn wrap-clj-req [app]
  (fn [req]
    (let [content-type ((req :headers) "content-type")]
      (clojure.contrib.logging/debug (str "content-type: " content-type))
      (if (= content-type clj-mime-type)
        (let [body (slurp (req :body))
	      reader (java.io.PushbackReader. (java.io.StringReader. body))
	      clj-req (read reader)]
	    (app (assoc req :clj-req clj-req)))
	(app req)))))
        	
;
;	(app req)))))



; Service implementation

; model

(def assets (atom { 1 {:id 1 :title "asset 1" :desc "interesting 1" }
		    2 {:id 2 :title "asset 2" :desc "interesting 2" }}))

; views

(defn assets-index-view [assets]
  (map
   (fn [[k v]]
     (assoc (dissoc v :desc)
       :details (gen-link :get-assets-id {:id (v :id)})))
   assets))

(defn asset-show-view [asset] asset)

; controllers

(defn assets-index [] (clj-resp (assets-index-view @assets)))

(defn asset-show [id] (clj-resp (asset-show-view (@assets id))))

(defn asset-create [asset] (clj-resp asset))

;(defn asset-create [asset]
;  (swap! *assets* assoc (asset :id) asset)
;  {:status 201 :headers {"Location" "absolute URL goes here"} :body nil})


(defroutes-and-generators routes
  (GET "/assets" [] (assets-index))
  (GET "/assets/:id" [id] (asset-show (Integer/parseInt id)))
  (POST "/assets" {req :clj-req} (asset-create req)))

(def application (-> routes
                     (logging/wrap-logging "")
		     wrap-clj-req
                     wrap-keyword-params
                     wrap-params))
 ; wrap-reload wrap-stacktrace

(defn start-server []
  (run-jetty (var application) {:port 8080 :join? false}))

(defn- start-dev-server [] (.start (Thread. start-server)))

(defn -main [& args] (start-server))


