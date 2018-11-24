(ns instagram-followers.routes)

(def routes
  [""
   {;; site
    "/" :site.top/index
    "/login" :site.login/get
    "/data" :site.data/index
    "/start-stop" :site.data/start-stop
    "/post" :site.data/post
    "/styles.css" :site/styles}])