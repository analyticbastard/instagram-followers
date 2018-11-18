(ns instagram-followers.routes)

(def routes
  [""
   {;; site
    "/" :site.top/index
    "/data" :site.data/index
    "/post" :site.data/post
    "/styles.css" :site/styles
    "/login" {["/" :category] :site.login/get}
    "/items" {["/" :item-id] :site.item/show}
    ;; api
    "/api"
    {"/books" :api.book/index}}])