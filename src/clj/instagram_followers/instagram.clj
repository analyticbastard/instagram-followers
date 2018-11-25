(ns instagram-followers.instagram
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.set :as set]
            [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]))

(def url "https://www.instagram.com/graphql/query/")
(def query-vars {:fetch_mutual true
                 :include_reel true
                 :first 50})

(def query-vars' {:include_chaining true
                  :include_reel true
                  :include_suggested_users true
                  :include_logged_out_extras false
                  :include_highlight_reels false})

(defn make-headers [cookie]
  {"Host"       "www.instagram.com"
   "User-Agent" "User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:63.0) Gecko/20100101 Firefox/63.0"
   "Accept" "*/*"
   "Accept-Language" "en-US,en;q=0.5"
   "Accept-Encoding" "gzip, deflate, br"
   "Referer" "https://www.instagram.com/bluepassereaux/followers/"
   "X-Instagram-GIS" "e40b18d59d278329722a069166f81d8f"
   "X-Requested-With" "XMLHttpRequest"
   "Connection" "keep-alive"
   "Cookie" cookie
   "Cache-Control" "max-age=0"
   "TE" "Trailers"})

(defn- make-query-params [user-data query-vars]
  {:query_hash (:query-hash user-data)
   :variables (json/encode (assoc query-vars :id (:id user-data)))}) ;(assoc query-vars :user_id (:id user-data))

(defn- make-request-data [headers]
  {:headers headers})

(defn- make-url [url query-params]
  (format "%s?%s" url (http/generate-query-string query-params)))

(defn- parse-result [response]
  (-> response
      :body
      (json/decode keyword)
      (get-in [:data :user :edge_followed_by])))

(defn request [url query-params headers]
  (let [response (http/get (make-url url query-params) (make-request-data headers))
        data (parse-result response)]
    {:control (assoc (set/rename-keys (:page_info data)
                                      {:has_next_page :next-page? :end_cursor :cursor})
                :size (:count data))
     :followers (map #(get % :node) (:edges data))}))

(defn get-followers [{:keys [user-data cookie npages]}]
  (loop [cursor nil
         cumm-followers []
         n (when npages (dec npages))]
    (let [next-vars (if cursor (assoc query-vars :after cursor) query-vars)
          {:keys [control followers]} (request url
                                               (make-query-params user-data next-vars)
                                               (make-headers @cookie))
          public-followers (remove :is_private followers)
          concat-followers (concat cumm-followers public-followers)]
      (if (and (:next-page? control)
               (or (not n) (pos? n)))
        (recur (:cursor control)
               concat-followers
               (when n (dec n)))
        concat-followers))))

(defn get-profile [{:keys [cookie]} {:keys [username] :as user}]
  (let [url (format "https://www.instagram.com/%s/?__a=1" username)
        data (http/get url
                       (make-request-data
                         (make-headers @cookie)))]
    (json/decode (:body data) keyword)))

(defn fetch-profile [user-profile]
  (get-in user-profile [:graphql :user :edge_owner_to_timeline_media :edges]))

(defn id [user]
  (get-in user [:node :id]))

(defn like [{:keys [cookie csrftoken]} post-id]
  (-> (format "https://www.instagram.com/web/likes/%s/like/" post-id)
      (http/post (make-request-data
                   (assoc (make-headers @cookie)
                     "X-CSRFToken" @csrftoken)))))

(defrecord Instagram [initial-cookie initial-token npages cookie csrftoken user-data initializing]
  component/Lifecycle
  (start [this]
    (assoc this :cookie (atom initial-cookie)
                :csrftoken (atom initial-token)
                :users (atom [])
                :initializing (atom false)))

  (stop [this]
    this))

(defn update-cookie! [component value]
  (reset! (:cookie component) value))

(defn update-csrftoken! [component value]
  (reset! (:csrftoken component) value))

(defn get-users [component]
  (-> component :users deref))

(defn initialize! [{:keys [initializing users] :as component}]
  (when-not (deref initializing)
    (reset! initializing true)
    (let [followers (get-followers component)]
      (log/info (str "Retrieved " (count followers) " followers"))
      (try (reset! users followers)
           (catch Exception e
             (log/info (str "Could not initialize: " e)))
           (finally
             (reset! initializing false))))))
