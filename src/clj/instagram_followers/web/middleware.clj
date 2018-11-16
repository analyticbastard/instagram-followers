(ns instagram-followers.web.middleware
  (:require [muuntaja.middleware :refer [wrap-format]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.util.response :as res]))

(defn wrap-not-found [handler]
  (fn [req]
    (if-let [res (handler req)]
      res
      (res/not-found "Resource not found."))))

(defn wrap [handler]
  (fn [req]
    (let [wrapped-handler
          (if (clojure.string/starts-with? (:uri req) "/api")
            ;; api
            (-> handler wrap-format (wrap-defaults api-defaults) wrap-not-found)
            ;; site
            (-> handler (wrap-defaults site-defaults) wrap-not-found))]
      (wrapped-handler req))))