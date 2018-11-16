(ns instagram-followers.routes)

(def routes
  [""
   {;; site
    "/" :site.top/index
    "/categories" {["/" :category] :site.category/show}
    "/items" {["/" :item-id] :site.item/show}
    ;; api
    "/api"
    {"/books" :api.book/index}}])