(ns instagram-followers.web.controllers
  (:require [cemerick.friend :as friend]
            [clojure.core.async :as a :refer [go go-loop >! >!! <! chan close! timeout alts!]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [ninjudd.eventual.server :refer [edn-events]]
            [garden.core :refer [css]]
            [instagram-followers
             [instagram :as instagram]
             [liker :as liker]
             [routes :as routes]
             [scheduler :as scheduler]
             [view :as view]]
            [instagram-followers.views
             [data :as data]
             [results :as results]
             [styles :as styles]
             [top :as top]]
            [instagram-followers.web
             [auth :as auth]
             [utils :as utils]]
            [ring.util.response :as res]
            [rum.core :as rum]))

(defrecord SiteLoginController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req]
                                   (utils/rum-ok (view/layout false (top/index))))))
  (stop [component] (dissoc component :controller)))

(defrecord SiteTopLevelController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req]
                                   (res/redirect (ffirst (filter (fn [[k v]] (= v :controller/index))
                                                                 (second routes/routes)))))))
  (stop [component] (dissoc component :controller)))

(defrecord SiteDataIndexController []
  component/Lifecycle
  (start [{:keys [scheduler like-handler] :as component}]
    (assoc component :controller (cemerick.friend/wrap-authorize
                                   (fn [req]
                                     (let [is-running? (boolean (some-> (scheduler/job scheduler) deref))]
                                       (->> (merge (liker/get-stats like-handler)
                                                   {:is-running? is-running?})
                                            data/index
                                            (view/layout true)
                                            utils/rum-ok)))
                                   #{::auth/user})))
  (stop [component] (dissoc component :controller)))

(defrecord SiteStartStopController []
  component/Lifecycle
  (start [{scheduler :scheduler sse :controller/sse :as component}]
    (assoc component :controller (fn [req]
                                   (if (boolean (some-> (scheduler/job scheduler) deref))
                                     (.disable scheduler)
                                     (.enable scheduler))
                                   (go (>! (:chan sse) {:is-running? (boolean (some-> (scheduler/job scheduler) deref))}))
                                   (utils/rum-ok {}))))
  (stop [component] (dissoc component :controller)))

(defrecord SitePostController []
  component/Lifecycle
  (start [{:keys [instagram] :as component}]
    (assoc component :controller (cemerick.friend/wrap-authorize
                                   (fn [{:keys [params] :as req}]
                                     (let [csrftoken (get params "one")
                                           cookie (get params "two")]
                                       (if (and (not (or (string/blank? csrftoken) (string/blank? cookie)))
                                                (re-find (re-pattern csrftoken) cookie))
                                         (do
                                           (instagram/update-csrftoken! instagram csrftoken)
                                           (instagram/update-cookie! instagram cookie)
                                           (utils/rum-ok (view/layout true (results/ok))))
                                         (utils/rum-ok (view/layout true (results/fail))))))
                                   #{::auth/user})))
  (stop [component] (dissoc component :controller)))

(defrecord SiteStylesController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req] (res/response (css (styles/styles))))))
  (stop [component] (dissoc component :controller)))

(defrecord SiteMainController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req]
                                   (res/response (slurp (io/resource "public/js/main.js"))))))
  (stop [component] (dissoc component :controller)))

(defrecord SiteJsController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [request]
                                   (some->> (:uri request)
                                            (format "public%s")
                                            io/resource
                                            slurp
                                            res/response))))
  (stop [component] (dissoc component :controller)))

(defrecord SiteSSEController []
  component/Lifecycle
  (start [component ]
    (let [events (chan)]
      (assoc component
        :chan events
        :controller (cemerick.friend/wrap-authorize
                      (fn [_] {:body events})
                      #{::auth/user}))))
  (stop [component] (dissoc component :controller)))

(defrecord SitePollerController []
  component/Lifecycle
  (start [{scheduler :scheduler like-handler :like-handler sse :controller/sse :as component}]
    (let [ch (chan)
          events (:chan sse)]
      (go-loop [prev-state {}]
        (let [state (merge prev-state
                           (liker/get-stats like-handler)
                           {:is-running? (boolean (some-> (scheduler/job scheduler) deref))})
              [_ p] (alts! [ch (timeout 1000)])]
          (when-not (= prev-state state)
            (>! events state))
          (if-not (= p ch)
            (recur state))))
      (assoc component :chan ch)))
  (stop [component]
    (close! (:chan component))
    (dissoc component :chan)))
