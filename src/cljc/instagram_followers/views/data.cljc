(ns instagram-followers.views.data
  (:require
    #?(:cljs [cljs-http.client :as http])
    [rum.core :as rum]))

(defn start-stop! [_]
  #?(:cljs (http/post "/start-stop")))

(rum/defc running-section
  < rum.core/static rum.core/reactive
  [{:keys [is-running?]}]
  [:article.tile.is-child.notification
   [:h1.title "Status"]
   [:div [:span (str "Application " (if is-running? "running" "stopped"))]]
   [:div.buttons.is-centered
    [:p.control
     [:a.button {:on-click start-stop! :class (str "button is-medium " (if is-running? "is-danger" "is-success"))}
      [:span.icon.is-medium [:i {:class (str "fas " (if is-running? "fa-pause" "fa-play"))}]]
      [:span (if is-running? "Stop" "Start")]]]]])

(rum/defc index
  < rum.core/static rum.core/reactive
  [{:keys [users likes is-running?] :as params}]
  [:div.tile.is-ancestor
   [:div.tile.is-vertical
    [:div.tile
     [:div.tile.is-parent
      [:article.tile.is-child.notification
       [:h1.title "Data"]
       [:form {:action "/post" :method "post"}
        [:input {:name "one" :type "login"}]
        [:textarea {:name "two"}]
        [:div.buttons.is-right
         [:button.button.is-medium.is-success {:type "submit"} "Submit"]]]]]
     [:div.tile.is-parent#running-section {:data-is-running is-running?}
      (running-section params)]]
    [:div.tile.is-parent
     [:article.tile.is-child.notification
      [:h1.title "Statistics"]
      [:div [:span (or users 0)] " users retrieved"]
      [:div [:span (or likes 0)] " likes given so far"]]]]])
