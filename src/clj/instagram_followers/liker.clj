(ns instagram-followers.liker
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [instagram-followers.instagram :as instagram]))

(def init-stats {:users 0 :likes 0})

(defn get-posts-ids [{:keys [max-posts max-likes]} user-profile]
  (->> (instagram/fetch-profile user-profile)
       (take max-posts)
       shuffle
       (take max-likes)
       (map instagram/id)))

(defn make-like-handler [{:keys [max-users max-likes interval stats instagram] :as this}]
  (fn ! []
    (try (if-let [users (seq (instagram/get-users instagram))]
           (let [num-users (rand-int max-users)
                 num-likes (rand-int max-likes)]
             (doseq [user (map #(% users) (repeat num-users rand-nth))
                     :let [profile (instagram/get-profile instagram user)
                           posts (get-posts-ids this profile)]]
               (doseq [post-id (map #(% posts) (repeat num-likes rand-nth))]
                 (Thread/sleep interval)
                 (instagram/like instagram post-id)))
             (swap! stats update :likes + num-likes)
             (log/info "Likes given in this round" num-likes)
             (log/info "Likes given in total" (-> stats deref :likes)))
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