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
             [endpoints :as endpoints]
             [middleware :as middleware]]
            [system.components
             [endpoint :refer [new-endpoint]]
             [handler :refer [new-handler]]
             [jetty :refer [map->WebServer]]
             [middleware :refer [new-middleware]]]
            [instagram-followers.web.auth :as auth]))

(defn new-system-map [profile]
  (apply component/system-map
         (concat [:logging (logging/map->Logging {})
                  :instagram (instagram/map->Instagram {})
                  :scheduler (scheduler/map->Scheduler {})
                  :like-handler (liker/map->Liker {})
                  :web (map->WebServer {})
                  :handler (new-handler :router :bidi)
                  :auth (auth/map->Auth {})
                  :middleware (new-middleware {:middleware [(partial middleware/wrap (get-in (config/config profile) [:middleware :secret]))]} #_{:middleware [wrap]})
                  :endpoint (new-endpoint endpoints/endpoint)]
                 [:site.top/index (controllers/map->SiteTopLevelController {})
                  :site.login/get (controllers/map->SiteLoginController {})
                  :site.data/index (controllers/map->SiteDataIndexController {})
                  :site.data/start-stop (controllers/map->SiteStartStopController {})
                  :site.data/post (controllers/map->SitePostController {})
                  :controllers/sse (controllers/map->SiteSSEController {})
                  :site/main (controllers/map->SiteMainController {})
                  :site/js (controllers/map->SiteJsController {})
                  :site/styles (controllers/map->SiteStylesController {})]
                 (when-not (= :dev profile)
                   [:nrepl (nrepl/map->NReplServer {:port 7888})]))))

(defn new-dependency-map [_]
  {:instagram [:logging]
   :scheduler [:like-handler :logging]
   :like-handler [:instagram]
   :handler [:endpoint :middleware]
   :middleware [:auth]
   :site.data/post [:instagram]
   :site.data/start-stop [:scheduler :site.data/index]
   :site.data/index [:scheduler :like-handler]
   :endpoint [:site.top/index :site.login/get :site.data/index :site/styles :site.data/post :site.data/start-stop
              :site/main :site/js :controllers/sse]
   :web [:handler]})

(defn new-system [profile]
  (-> (new-system-map profile)
      (config/configure profile)
      (component/system-using (new-dependency-map profile))))