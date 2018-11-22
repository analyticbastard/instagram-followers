(ns instagram-followers.scheduler
  (:require [bonney.core :as bonney]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [instagram-followers.liker :as liker]))

(defn- wrap-errors [{:keys [active? job]} handler]
  (fn []
    (try
      (handler)
      (reset! active? true)
      (catch Exception _
        (reset! active? false)
        (swap! job bonney/kill)))))

(defn schedule! [{:keys [interval pool] :as component} handler]
  (bonney/every interval
                (wrap-errors component handler)
                pool
                :error-fn (fn [e] (log/info e))))

(defprotocol Activable
  (enable [_])
  (disable [_]))

(defrecord Scheduler [interval like-handler instagram]
  component/Lifecycle
  (start [{:keys [active?] :as this}]
    (cond-> this
            (or (not active?) (not @active?))
            (assoc :pool (bonney/create-pool :threads 1 :desc "Cron")
                   :job (atom nil)
                   :initialized (atom false)
                   :active? (atom false))))

  (stop [{:keys [job] :as this}]
    (cond-> this
            @job (-> disable
                     (update :pool bonney/shutdown))))

  Activable
  (enable [{:keys [job] :as this}]
    (cond-> this
            (not @job) (update :job reset! (schedule! this (liker/get-handler like-handler)))))

  (disable [{:keys [job] :as this}]
    (cond-> this
            @job (update :job swap! (comp (constantly nil) bonney/kill)))))

(defn is-running? [component]
  (boolean (-> component :job deref)))
