(ns clj-rest-svc.logging
  (:use clojure.pprint
        [clojure.contrib.logging :only [debug]]))

(def static-file-regex
     #".*\.(css|js|png|jpg|gif|ico)$")

(defn wrap-logging [handler prefix]
  (fn [request]
    (if (re-find static-file-regex (:uri request))
      (debug (str prefix (:uri request)))
      (debug (str prefix " " (:request-method request) " " (:uri request)
		  "\n\tParameters: " (with-out-str (pprint (:params request)))
		  "\n\tRequest: " (with-out-str (pprint request)))))
    (let [response (handler request)]
      (if-not (re-find static-file-regex (:uri request))
	(debug (str prefix " Response: " (with-out-str (pprint response)))))
      response)))	
