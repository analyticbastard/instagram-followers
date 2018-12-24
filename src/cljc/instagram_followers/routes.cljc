(ns instagram-followers.routes
  (:require bidi.ring))

(def routes
  [""
   {;; site
    "/"           :controller/index
    "/login"      :controller/get
    "/data"       :controller/index
    "/start-stop" :controller/start-stop
    "/post"       :controller/post
    "/styles.css" :controller/styles
    "/sse"        :controller/sse
    "/js"         [["/main.js" :controller/main]
                   [true :controller/js]]}])
