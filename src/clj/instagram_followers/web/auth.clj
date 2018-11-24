(ns instagram-followers.web.auth
  (:require [cemerick.friend.credentials :refer (hash-bcrypt)]
            [com.stuartsierra.component :as component]))

(def users (atom {}))

(set [#{::admin ::user}])

(derive ::admin ::user)


(defrecord Auth [username password]
  component/Lifecycle
  (start [this]
    (reset! users {username {:username username
                             :password (hash-bcrypt password)
                             :pin "1234" ;; only used by multi-factor
                             :roles #{::user}}})
    this)

  (stop [this]
    this))