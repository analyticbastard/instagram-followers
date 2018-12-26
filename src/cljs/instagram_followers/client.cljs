(ns instagram-followers.client
  (:require [bidi.bidi :refer [match-route]]
            [citrus.core :as citrus]
            [cljs.core.async :refer [go <!]]
            [instagram-followers.flow :as flow]
            [instagram-followers.routes :refer [routes]]
            [instagram-followers.view :as view]
            [rum.core :as rum]))

(enable-console-print!)

(if goog.DEBUG
  (println "Start debug mode :)"))

(defmulti control (fn [event] event))

(defmethod control :set [_ status _]
  {:state (first status)})

(defonce reconciler
         (citrus/reconciler
           {:state view/status
            :controllers {:status control}}))

;;make a request to listen for new events on the server
(defonce es (js/EventSource. "/sse"))

;;just print them out could swap into an atom and visualize with html component
(.addEventListener es "message" (fn [e]
                                  (let [data (cljs.reader/read-string (.-data e))]
                                    (swap! view/status merge data)
                                    (citrus/broadcast-sync! reconciler :set data))))


(when-let [app (js/document.getElementById "page")]
  (rum/hydrate (view/data-page reconciler) app))

(defn on-js-reload []
  (swap! flow/state update-in [:__figwheel_counter] inc))