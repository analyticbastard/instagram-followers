(ns instagram-followers.system
  (:require [com.stuartsierra.component :as component]
            [instagram-followers
             [config :as config]
             [instagram :as instagram]
             [liker :as liker]
             [logging :as logging]
             [nrepl :as nrepl]
             [scheduler :as scheduler]]
            [instagram-followers.web
             [controllers :as controllers]
             [endpoints :as endpoints]]
            [ring.middleware
             [multipart-params :refer [wrap-multipart-params multipart-params-request]]
             [params :refer [wrap-params]]]
            [system.components
             [endpoint :refer [new-endpoint]]
             [handler :refer [new-handler]]
             [jetty :refer [new-jetty]]
             [middleware :refer [new-middleware]]]))

(defn new-system-map [profile]
  (apply component/system-map
         (concat [:logging (logging/map->Logging {})
                  :instagram (instagram/map->Instagram {})
                  :scheduler (scheduler/map->Scheduler {})
                  :like-handler (liker/map->Liker {})
                  :web (new-jetty :port 8080)
                  :handler (new-handler :router :bidi)
                  :middleware (new-middleware {:middleware [wrap-params]} #_{:middleware [wrap]})
                  :endpoint (new-endpoint endpoints/endpoint)]
                 [:site.top/index (controllers/map->SiteTopIndexController {})
                  :site.data/index (controllers/map->SiteDataIndexController {})
                  :site.data/start-stop (controllers/map->SiteStartStopController {})
                  :site.data/post (controllers/map->SitePostController {})
                  :site/styles (controllers/map->SiteStylesController {})]
                 (when-not (= :dev profile)
                   [:nrepl (nrepl/map->NReplServer {:port 7888})]))))

(defn new-dependency-map [_]
  {:instagram [:logging]
   :scheduler [:like-handler :logging]
   :like-handler [:instagram]
   :handler [:endpoint :middleware]
   :site.data/post [:instagram]
   :site.data/start-stop [:scheduler :site.data/index]
   :site.data/index [:scheduler]
   :endpoint [:site.top/index :site.data/index :site/styles :site.data/post :site.data/start-stop]
   :web [:handler]})

(defn new-system [profile]
  ;(logging/set-default-uncaught-exception-handler)
  (-> (new-system-map profile)
      (config/configure profile)
      (component/system-using (new-dependency-map profile))))