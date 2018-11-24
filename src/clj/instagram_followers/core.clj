(ns instagram-followers.core
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component.repl :refer [reset set-init start stop system]]
            [instagram-followers
             [instagram :as instagram]
             [system :as system]])
  (:gen-class))

(defn -main [& _]
  (log/info "Starting instagram-followers")
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn [] (stop))))
  (set-init (fn [_] (system/new-system :prod)))
  (start))
