(ns instagram-followers.web.utils
  (:require [ring.util.response :as res]
            [rum.core :as rum]))

(defn- html-response [resp]
  (assoc-in resp [:headers "Content-Type"] "text/html;charset=utf-8"))

(defn rum-ok [coll]
  (-> (res/response (rum/render-html coll))
      html-response))

(defn rum-not-found [coll]
  (-> (res/not-found (rum/render-static-markup coll))
      html-response))
