(ns instagram-followers.web.middleware
  (:require [cemerick.friend :as friend]
            [cemerick.friend
             [workflows :as workflows]
             [credentials :as creds]]
            [instagram-followers.view :as view]
            [instagram-followers.web
             [auth :as auth]
             [utils :as utils]]
            [muuntaja.middleware :refer [wrap-format]]
            [ring.middleware
             [content-type :refer [wrap-content-type]]
             [cookies :refer [wrap-cookies]]
             [keyword-params :refer [wrap-keyword-params]]
             [multipart-params :refer [wrap-multipart-params]]
             [nested-params :refer [wrap-nested-params]]
             [params :refer [wrap-params]]
             [session :refer [wrap-session]]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.util.response :as res]))

(defn wrap-not-found [handler]
  (fn [req]
    (if-let [res (handler req)]
      res
      (res/not-found "Resource not found."))))

(defn wrap-authenticate [handler]
  (friend/authenticate
    handler
    {:allow-anon?          true
     :login-uri            "/login"
     :default-landing-uri  "/data"
     :unauthorized-handler #(utils/rum-not-found (view/layout [:h2 "You do not have sufficient privileges to access " (:uri %)]))
     :credential-fn        #(creds/bcrypt-credential-fn @auth/users %)
     :workflows            [(workflows/interactive-form)]}))

(defn wrap [secret handler]
  (fn [req]
    (let [wrapped-handler (-> handler
                              wrap-authenticate
                              wrap-params
                              wrap-keyword-params
                              wrap-nested-params
                              wrap-multipart-params
                              wrap-cookies
                              (wrap-session {:store (cookie-store {:key secret})}))]
      (wrapped-handler req))))
