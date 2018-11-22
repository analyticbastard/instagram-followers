(ns instagram-followers.scheduler
  (:require [bonney.core :as bonney]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [instagram-followers.liker :as liker]))

(def kill-and-clear! (comp (constantly nil) bonney/kill))

(defn- wrap-errors [{:keys [job]} handler]
  (fn []
    (when-not (try (handler)
                   (catch Exception _ false))
      (swap! job kill-and-clear!))))

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
  (start [{:keys [job] :as this}]
    (cond-> this
            (or (not job) (not @job))
            (assoc :pool (bonney/create-pool :threads 1 :desc "Cron")
                   :job (atom nil)
                   :initialized (atom false))))

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
            @job (update :job swap! kill-and-clear!))))

(defn is-running? [component]
  (boolean (-> component :job deref)))
