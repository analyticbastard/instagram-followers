(ns instagram-followers.scheduler
  (:require [com.stuartsierra.component :as component]
            [bonney.core :as bonney]))

(defprotocol Activable
  (enable [_])
  (disable [_]))

(defrecord Scheduler [average handler]
  component/Lifecycle
  (start [{:keys [active?] :as this}]
    (cond-> this
            (or (not active?) (not @active?))
            (assoc :pool (bonney/create-pool :threads 1 :desc "Cron")
                   :job (atom nil)
                   :active? (atom false))))

  (stop [{:keys [active?] :as this}]
    (cond-> this
            @active? (-> disable
                         (update :pool bonney/shutdown))))

  Activable
  (enable [{:keys [active? pool] :as this}]
    (cond-> this
            (not @active?) (-> (update :job reset! (bonney/every average #(println "XXX") pool))
                               (update :active? reset! true))))

  (disable [{:keys [active?] :as this}]
    (cond-> this
            @active? (-> (update :job swap! bonney/kill)
                         (update :active? reset! false)))))
