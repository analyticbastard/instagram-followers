(ns instagram-followers.web.endpoints
  (:require [instagram-followers.routes :refer [routes]]
            [bidi.ring :as br]))

(defn ring-handler? [var]
  (contains? (meta var) :ring-handler))

(defn ring-handlers []
  (->> (all-ns)
       (mapcat ns-interns)
       (map second)
       (filter ring-handler?)))

(defn match-handler [k]
  (->> (ring-handlers)
       (filter #(= (:ring-handler (meta %)) k))
       first))

(extend-protocol br/Ring
  clojure.lang.Keyword
  (request [k req _]
    (let [handler (match-handler k)]
      (handler req))))

(defn keyword->handler
  "find ring handler from m(ap) argument."
  [m]
  (extend-protocol br/Ring
    clojure.lang.Keyword
    (request [k req _]
      (let [handler (get-in m [k :controller])]
        (handler req)))))

(defn endpoint [component]
  (keyword->handler component) ;; FIXME: Second responsibility for this function...
  routes)