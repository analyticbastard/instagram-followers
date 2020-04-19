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
                  :middleware (new-middleware {:middleware [(partial middleware/wrap (get-in (config/config profile)
                                                                                             [:middleware :secret]))]})
                  :endpoint (new-endpoint endpoints/endpoint)]
                 [:controller/index (controllers/map->SiteTopLevelController {})
                  :controller/get (controllers/map->SiteLoginController {})
                  :controller/index (controllers/map->SiteDataIndexController {})
                  :controller/start-stop (controllers/map->SiteStartStopController {})
                  :controller/post (controllers/map->SitePostController {})
                  :controller/sse (controllers/map->SiteSSEController {})
                  :poller (controllers/map->SitePollerController {})
                  :controller/main (controllers/map->SiteMainController {})
                  :controller/js (controllers/map->SiteJsController {})
                  :controller/styles (controllers/map->SiteStylesController {})]
                 (when-not (= :dev profile)
                   [:nrepl (nrepl/map->NReplServer {:port 7888})]))))

(defn new-dependency-map [_]
  {:instagram             [:logging]
   :scheduler             [:like-handler :logging]
   :like-handler          [:instagram]
   :handler               [:endpoint :middleware]
   :middleware            [:auth]
   :controller/post       [:instagram]
   :controller/start-stop [:scheduler :controller/index :controller/sse]
   :controller/index      [:scheduler :like-handler]
   :poller                [:scheduler :like-handler :controller/sse]
   :endpoint              [:controller/index :controller/get :controller/index :controller/styles :controller/post :controller/start-stop
              :controller/main :controller/js :controller/sse]
   :web                   [:handler]})

(defn new-system [profile]
  (-> (new-system-map profile)
      (config/configure profile)
      (component/system-using (new-dependency-map profile))))