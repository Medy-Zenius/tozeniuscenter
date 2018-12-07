(ns cbtonline.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            ;[ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [noir.util.middleware :as noir-middleware]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [cbtonline.routes.home :refer [home-routes]]))

(defn init []
  (println "icbl is starting"))

(defn destroy []
  (println "icbl is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (noir-middleware/app-handler
       [home-routes
        app-routes
        ]
        :ring-defaults (assoc-in site-defaults [:security :anti-forgery] false)))
