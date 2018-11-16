(ns instagram-followers.controllers
  (:require [com.stuartsierra.component :as component]
            [ring.util.response :as res]
            [rum.core :as rum]))

(defn- html-response [resp]
  (assoc-in resp [:headers "Content-Type"] "text/html;charset=utf-8"))

(defn rum-ok [coll]
  (-> (res/response (rum/render-static-markup coll))
      html-response))

(defrecord SiteTopIndexController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req] (rum-ok (layout (top/index))))))
  (stop [component] (dissoc component :controller)))

(defrecord SiteCategoryShowController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req]
                                   (let [category (get-in req [:params :category])]
                                     (rum-ok
                                       (category/show
                                         {:category category
                                          :items (item-model/find-by-category category)}))))))
  (stop [component] (dissoc component :controller)))

(defrecord SiteItemShowController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req]
                                   (let [item-id (read-string (get-in req [:params :item-id]))
                                         item (item-model/find-by-id item-id)]
                                     (res/rum-ok (item/show {:item item}))))))
  (stop [component] (dissoc component :controller)))

(defrecord ApiBookIndexController []
  component/Lifecycle
  (start [component]
    (assoc component :controller (fn [req] {:status 200 :body [{:title "hoge" :author "fuga"}]})))
  (stop [component] (dissoc component :controller)))