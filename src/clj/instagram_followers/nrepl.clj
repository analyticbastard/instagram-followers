(ns instagram-followers.nrepl
  (:require [clojure.tools.nrepl.server :as nrserver]
            [com.stuartsierra.component :refer [Lifecycle]]))

(def default-nrepl-port 7777)

(defrecord NReplServer [port]
  Lifecycle

  (start [this]
    (let [bind-addr "0.0.0.0"
          server (nrserver/start-server :port port
                                        :bind bind-addr)]
      (assoc this :server server)))

  (stop [{:keys [server] :as this}]
    (when server
      (.close server)
      (dissoc this :server))))

(defn make-nrepl-server [{:keys [nrepl-port]
                          :or {nrepl-port default-nrepl-port}}]
  (NReplServer. nrepl-port))