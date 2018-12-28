(ns instagram-followers.client
  (:require [bidi.bidi :refer [match-route]]
            [citrus.core :as citrus]
            [cljs.core.async :refer [go <!]]
            [cljs.reader :as reader]
            [instagram-followers.flow :as flow]
            [instagram-followers.routes :refer [routes]]
            [instagram-followers.view :as view]
            [rum.core :as rum]))

(enable-console-print!)

(if goog.DEBUG
  (println "Start debug mode :)"))

(def config
  (let [node-list-object (js/document.getElementsByName "config")
        node (.item node-list-object 0)
        content (.getAttribute node "content")]
    (reader/read-string content)))

(defn dispatch [event] event)

(defmulti control-is-running dispatch)

(defmethod control-is-running :init [a [is-running?]]
  {:state (if-not (nil? is-running?)
            (first is-running?)
            (:is-running? config))})

(defmethod control-is-running :set [a b c]
  {:state (first b)})


(defmulti control-users dispatch)

(defmethod control-users :set [a b c]
  {:state (first b)})

(defmethod control-users :init [a [users]]
  {:state (if-not (nil? users)
            (first users)
            (:users config))})

(defonce reconciler
         (citrus/reconciler
           {:state view/status
            :controllers {:is-running? control-is-running
                          :users       control-users}}))

(defonce init-ctrl (citrus/broadcast-sync! reconciler :init))

;;make a request to listen for new events on the server
(defonce es (js/EventSource. "/sse"))

;;just print them out could swap into an atom and visualize with html component
(.addEventListener es "message" (fn [e]
                                  (let [{:keys [is-running? users]} (cljs.reader/read-string (.-data e))]
                                    (when-not (nil? is-running?)
                                      (citrus/dispatch! reconciler :is-running? :set is-running?))
                                    (when-not (nil? users)
                                      (citrus/dispatch! reconciler :users :set users)))))


(when-let [app (js/document.getElementById "page")]
  (rum/hydrate (view/data-page reconciler) app))

(defn on-js-reload []
  (swap! flow/state update-in [:__figwheel_counter] inc))