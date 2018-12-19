(ns instagram-followers.web.controllers
  (:require [cemerick.friend :as friend]
            [clojure.core.async :as a :refer [go >! >!! <! chan close! timeout]]
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
                                   (utils/rum-ok (view/layout (top/index))))))
  (stop [component] (dissoc component :controller)))

(defrecord SiteTopLevelController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req]
                                   (res/redirect (ffirst (filter (fn [[k v]] (= v :site.data/index))
                                                                 (second routes/routes)))))))
  (stop [component] (dissoc component :controller)))

(defrecord SiteDataIndexController []
  component/Lifecycle
  (start [{:keys [scheduler like-handler] :as component}]
    (assoc component :controller (cemerick.friend/wrap-authorize
                                   (fn [req]
                                     (let [is-running? (boolean (some-> (scheduler/job scheduler) deref))]
                                       (-> (merge (liker/get-stats like-handler)
                                                  {:is-running? is-running?})
                                           data/index
                                           view/layout
                                           utils/rum-ok)))
                                   #{::auth/user})))
  (stop [component] (dissoc component :controller)))

(defrecord SiteStartStopController []
  component/Lifecycle
  (start [{scheduler :scheduler sse :controllers/sse :as component}]
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
                                           (utils/rum-ok (view/layout (results/ok))))
                                         (utils/rum-ok (view/layout (results/fail))))))
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
    (assoc component :controller (fn [{params :params}]
                                   (res/response (slurp (io/resource
                                                          (first (string/split
                                                                   (format "public/js/out/%s"
                                                                           (string/join "/" (vals (select-keys params [:one :two :three :four :five]))))
                                                                   #"\?"))))))))
  (stop [component] (dissoc component :controller)))

(defrecord SiteSSEController []
  component/Lifecycle
  (start [{:keys [scheduler] :as component}]
    (let [events (chan)]
      (assoc component
        :chan events
        :controller (cemerick.friend/wrap-authorize
                      (fn [req]
                        (go (>! events {:is-running? (boolean (some-> (scheduler/job scheduler) deref))}))
                        {:body events})
                      #{::auth/user}))))
  (stop [component] (dissoc component :controller)))
