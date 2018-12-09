(ns instagram-followers.liker
  (:require [clojure.core.async :refer [go go-loop timeout <! >!! chan close!]]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [instagram-followers.instagram :as instagram]))

(def init-stats {:users 0 :likes 0})

(defn get-posts-ids [{:keys [max-posts max-likes]} user-profile]
  (->> (instagram/fetch-profile user-profile)
       (take max-posts)
       shuffle
       (take max-likes)
       (map instagram/id)))

(defn- create-liker-service [instagram max-buff interval]
  (let [ch (chan max-buff)]
    (go-loop []
      (when-let [post-id (<! ch)]
        (instagram/like instagram post-id)
        (<! (timeout interval))
        (recur)))
    ch))

(defn- like-post! [ch post-id]
  (>!! ch post-id))

(defn- sleep [interval]
  (try (Thread/sleep interval)
       (catch InterruptedException _)))

(defn make-like-handler [{:keys [max-users max-likes interval stats instagram] :as this}]
  (fn ! []
    (try (if-let [users (seq (instagram/get-users instagram))]
           (let [ch (create-liker-service instagram (inc (* max-users max-likes)) interval)
                 num-users (rand-int max-users)]
             (swap! stats assoc :users (count users))
             (doseq [user (map #(% users) (repeat num-users rand-nth))
                     :let [profile (instagram/get-profile instagram user)
                           posts (get-posts-ids this profile)
                           num-likes (min (count posts) (inc (rand-int max-likes)))
                           post-ids (map #(% posts) (repeat num-likes rand-nth))]]
               (doseq [post-id (set post-ids)]
                 (like-post! ch post-id)
                 (swap! stats update :likes + num-likes))
               (sleep interval))
             (close! ch)
             (log/info "Likes given in total" (-> stats deref :likes))
             true)
           (do
             (instagram/initialize! instagram)
             (!)))
         (catch Exception e
           (log/info "Error liking followers")
           (log/info e)))))

(defrecord Liker [max-posts max-users max-likes instagram]
  component/Lifecycle
  (start [this]
    (as-> this $
          (assoc $ :stats (atom init-stats))
          (assoc $ :handler (make-like-handler $))))

  (stop [this]
    (-> (update this reset! :stats init-stats)
        (dissoc :handler))))

(defn get-handler [component]
  (:handler component))

(defn get-stats [component]
  (-> component :stats deref))