(ns instagram-followers.web.middleware
  (:require [cemerick.friend :as friend]
            [cemerick.friend
             [workflows :as workflows]
             [credentials :as creds]]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [instagram-followers.view :as view]
            [instagram-followers.web
             [auth :as auth]
             [utils :as utils]]
            [muuntaja.middleware :refer [wrap-format]]
            [ninjudd.eventual.server :refer [json-events]]
            [ring.middleware
             [content-type :refer [wrap-content-type]]
             [cookies :refer [wrap-cookies]]
             [keyword-params :refer [wrap-keyword-params]]
             [multipart-params :refer [wrap-multipart-params]]
             [nested-params :refer [wrap-nested-params]]
             [params :refer [wrap-params]]
             [session :refer [wrap-session]]]
            [ring.core.protocols :refer [StreamableResponseBody]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.util.response :as res])
  (:import (clojure.core.async.impl.channels ManyToManyChannel)
           (java.io IOException OutputStream)))

(extend-type ManyToManyChannel
  StreamableResponseBody
  (write-body-to-stream [channel _ ^OutputStream output-stream]
    (async/go
      (try
        (loop []
          (if-let [msg (async/<! channel)]
            (do
              (if-not (= :flush msg)
                (doto output-stream
                  (.write ^bytes msg)
                  (.flush))
                (.flush output-stream))
              (recur))
            (.close output-stream)))
        (catch IOException e
          (async/close! channel))
        (catch Exception e
          (log/error "Encountered error sending server event." e))))))

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

(defn wrap-server-sent [handler]
  (fn [request]
    (let [{:keys [body] :as response} (handler request)]
      (if (= ManyToManyChannel (class body))
        (json-events body)
        response))))

(defn wrap-handler [handler secret]
  (-> handler
      wrap-authenticate
      wrap-params
      wrap-keyword-params
      wrap-nested-params
      wrap-multipart-params
      wrap-cookies
      (wrap-session {:store (cookie-store {:key secret})})))

(defn wrap [secret handler]
  (fn
    ([req]
     ((wrap-handler handler secret) req))
    ([req resp raise]
     (try
       (let [wrapped-handler (-> handler
                                 wrap-server-sent
                                 (wrap-handler secret))]
         (resp (wrapped-handler req)))
       (catch Exception e
         (println e)
         (raise e))))))
