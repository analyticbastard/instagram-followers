(ns instagram-followers.routes
  (:require bidi.ring))

(def routes
  [""
   {;; site
    "/"           :site.top/index
    "/login"      :site.login/get
    "/data"       :site.data/index
    "/start-stop" :site.data/start-stop
    "/post"       :site.data/post
    "/styles.css" :site/styles
    "/sse"        :controllers/sse
    "/js"         {"/main.js" :site/main
                   ["/out/" :one] :site/js
                   ["/out/" :one "/" :two] :site/js
                   ["/out/" :one "/" :two "/" :three] :site/js
                   ["/out/" :one "/" :two "/" :three "/" :four] :site/js
                   ["/out/" :one "/" :two "/" :three "/" :four "/" :five] :site/js
                   ["/out/" :one "/" :two "/" :three "/" :four "/" :five "/" :six] :site/js
                   }}])