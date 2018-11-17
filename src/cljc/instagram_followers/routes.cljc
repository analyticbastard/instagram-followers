(ns instagram-followers.routes)

(def routes
  [""
   {;; site
    "/" :site.top/index
    "/login" {["/" :category] :site.login/get}
    "/items" {["/" :item-id] :site.item/show}
    ;; api
    "/api"
    {"/books" :api.book/index}}])