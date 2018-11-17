(ns instagram-followers.system
  (:require [com.stuartsierra.component :as component]
            [instagram-followers
             [config :as config]
             [scheduler :as scheduler]
             [instagram :as instagram]
             [logging :as logging]
             [nrepl :as nrepl]]
            [instagram-followers.web
             [controllers :as controllers]
             [endpoints :as endpoints]]
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
                  :web (new-jetty :port 8080)
                  :handler (new-handler :router :bidi)
                  :middleware (new-middleware {} #_{:middleware [wrap]})
                  :endpoint (new-endpoint endpoints/endpoint)]
                 [:site.top/index (controllers/map->SiteTopIndexController {})]
                 (when-not (= :dev profile)
                   [:nrepl (nrepl/map->NReplServer {:port 7888})]))))

(defn new-dependency-map [_]
  {:instagram [:logging]
   :scheduler {:handler :instagram
               :logging :logging}
   :handler [:endpoint :middleware]
   :endpoint [:site.top/index]
   :web [:handler]})

(defn new-system [profile]
  ;(logging/set-default-uncaught-exception-handler)
  (-> (new-system-map profile)
      (config/configure profile)
      (component/system-using (new-dependency-map profile))))