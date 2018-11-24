(ns instagram-followers.web.controllers
  (:require [cemerick.friend :as friend]
            [com.stuartsierra.component :as component]
            [garden.core :refer [css]]
            [instagram-followers
             [instagram :as instagram]
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
            [clojure.string :as string]))

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
  (start [{:keys [scheduler] :as component}]
    (assoc component :controller (cemerick.friend/wrap-authorize
                                   (fn [req]
                                     (utils/rum-ok (view/layout (data/index (scheduler/is-running? scheduler)))))
                                   #{::auth/user})))
  (stop [component] (dissoc component :controller)))

(defrecord SiteStartStopController []
  component/Lifecycle
  (start [{:keys [scheduler] :as component}]
    (assoc component :controller (fn [req]
                                   (if (scheduler/is-running? scheduler)
                                     (.disable scheduler)
                                     (.enable scheduler))
                                   (res/redirect (ffirst (filter (fn [[k v]] (= v :site.data/index))
                                                                 (second routes/routes)))))))
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
