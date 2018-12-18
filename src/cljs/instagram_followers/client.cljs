(ns instagram-followers.client
  (:require [bidi.bidi :refer [match-route]]
            [cljs.core.async :refer [go <!]]
            [instagram-followers.flow :as flow]
            [instagram-followers.routes :refer [routes]]
            [instagram-followers.view :as view]
            [rum.core :as rum]))

(enable-console-print!)

(if goog.DEBUG
  (println "Start debug mode :)"))

;;make a request to listen for new events on the server
(defonce es (js/EventSource. "/sse"))

;;just print them out could swap into an atom and visualize with html component
(.addEventListener es "message" (fn [e]
                                  (let [data (cljs.reader/read-string (.-data e))
                                        is-running? (:is-running? data)]
                                    (reset! view/status is-running?))))

(when-let [app (js/document.getElementById "running-section")]
  (rum/hydrate (view/running-section flow/state) app))

(defn on-js-reload []
  (swap! flow/state update-in [:__figwheel_counter] inc))