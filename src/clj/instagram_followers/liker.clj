(ns instagram-followers.liker
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [instagram-followers.instagram :as instagram]))

(defn get-posts-ids [{:keys [post-newest max-likes]} user-profile]
  (->> (instagram/fetch-profile user-profile)
       (take post-newest)
       shuffle
       (take max-likes)
       (map instagram/id)))

(defn make-like-handler [{:keys [max-users max-likes interval instagram] :as this}]
  (fn []
    (try (if-let [users (seq (instagram/get-users instagram))]
           (doseq [user (map #(% users) (repeat (rand-int max-users) rand-nth))
                   :let [profile (instagram/get-profile instagram user)
                         posts (get-posts-ids this profile)]]
             (doseq [post-id (map #(% posts) (repeat (rand-int max-likes) rand-nth))]
               (Thread/sleep interval)
               (instagram/like instagram post-id)))
           (instagram/initialize! instagram))
         (catch Exception e
           (log/info "Error liking followers")
           (log/info e)))))

(defrecord Liker [post-newest max-users max-likes instagram]
  component/Lifecycle
  (start [this]
    (assoc this :handler (make-like-handler this)))

  (stop [this]
    (dissoc :handler)))

(defn get-handler [component]
  (:handler component))
