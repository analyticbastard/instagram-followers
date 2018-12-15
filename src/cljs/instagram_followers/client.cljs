(ns instagram-followers.client
  (:require [bidi.bidi :refer [match-route]]
            [instagram-followers.flow :as flow]
            [instagram-followers.routes :refer [routes]]
            [instagram-followers.view :as view]
            [rum.core :as rum]))

(enable-console-print!)

(if goog.DEBUG
  (println "Start debug mode :)"))

(when-let [app (js/document.getElementById "running-section")]
  (rum/hydrate (view/running-section flow/state) app))

(defn on-js-reload []
  (swap! flow/state update-in [:__figwheel_counter] inc))