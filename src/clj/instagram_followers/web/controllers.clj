(ns instagram-followers.web.controllers
  (:require [com.stuartsierra.component :as component]
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
            [ring.util.response :as res]
            [rum.core :as rum]
            [clojure.string :as string]))

(defn- html-response [resp]
  (assoc-in resp [:headers "Content-Type"] "text/html;charset=utf-8"))

(defn rum-ok [coll]
  (-> (res/response (rum/render-static-markup coll))
      html-response))

(defrecord SiteTopIndexController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req] (rum-ok (view/layout (top/index))))))
  (stop [component] (dissoc component :controller)))

(defrecord SiteDataIndexController []
  component/Lifecycle
  (start [{:keys [scheduler] :as component}]
    (assoc component :controller (fn [req]
                                   (rum-ok (view/layout (data/index (scheduler/is-running? scheduler)))))))
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
    (assoc component :controller (fn [{:keys [params] :as req}]
                                   (let [csrftoken (get params "one")
                                         cookie (get params "two")]
                                     (if (and (not (or (string/blank? csrftoken) (string/blank? cookie)))
                                              (re-find (re-pattern csrftoken) cookie))
                                       (do
                                         (instagram/update-csrftoken! instagram csrftoken)
                                         (instagram/update-cookie! instagram cookie)
                                         (rum-ok (view/layout (results/ok))))
                                       (rum-ok (view/layout (results/fail))))))))
  (stop [component] (dissoc component :controller)))

(defrecord SiteStylesController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req] (res/response (css (styles/styles))))))
  (stop [component] (dissoc component :controller)))

#_(defrecord SiteCategoryShowController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req]
                                   (let [category (get-in req [:params :category])]
                                     (rum-ok
                                       (category/show
                                         {:category category
                                          :items (item-model/find-by-category category)}))))))
  (stop [component] (dissoc component :controller)))

#_(defrecord SiteItemShowController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req]
                                   (let [item-id (read-string (get-in req [:params :item-id]))
                                         item (item-model/find-by-id item-id)]
                                     (res/rum-ok (item/show {:item item}))))))
  (stop [component] (dissoc component :controller)))

#_(defrecord ApiBookIndexController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req] {:status 200 :body [{:title "hoge" :author "fuga"}]})))
  (stop [component] (dissoc component :controller)))