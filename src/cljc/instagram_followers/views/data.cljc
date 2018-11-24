(ns instagram-followers.views.data)

(defn index [is-running?]
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
     [:div.tile.is-parent
      [:article.tile.is-child.notification
       [:h1.title "Status"]
       [:div [:span
              (str "Application " (if is-running? "running" "stopped"))]]
       [:form {:action "/start-stop"}
        [:div.buttons.is-centered
         [:p.control
          [:button {:type "submit" :class (str "button is-medium " (if is-running? "is-danger" "is-success"))}
           [:span.icon-is-medium [:i {:class (str "fas " (if is-running? "fa-pause" "fa-play"))}]]
           [:span (if is-running? "Stop" "Start")]]]]]]]]
    [:div.tile.is-parent
     [:article.tile.is-child.notification
      [:h1.title "Statistics"]
      [:span [:span "10"] " likes given to " [:span "4"] " users"]]]]])