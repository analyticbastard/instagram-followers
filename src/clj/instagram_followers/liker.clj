(ns instagram-followers.liker
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [instagram-followers.instagram :as instagram]))

(defn make-like-handler [{:keys [max-users max-likes active instagram]}]
  (fn []
    (try (if-let [users (seq (instagram/get-users instagram))]
           (doseq [user (map #(% users) (repeat (rand-int max-users) rand-nth))
                   :let [profile (instagram/get-profile instagram user)
                         posts (instagram/get-posts-ids instagram profile)]]
             (doseq [post-id (map #(% posts) (repeat (rand-int max-likes) rand-nth))]
               (instagram/like instagram post-id)))
           (instagram/initialize! instagram))
         (reset! active true)
         (catch Exception _
           (log/info "Error liking followers")
           (reset! active false)))))

(defrecord Liker [instagram]
  component/Lifecycle
  (start [this]
    (as-> (assoc this :active (atom false)) $
          (assoc $ :handler (make-like-handler $))))

  (stop [this]
    (-> (update this :active reset! false)
        (dissoc :handler))))

(defn get-handler [component]
  (:handler component))

(defn is-active? [{:keys [active]}]
  (deref active))