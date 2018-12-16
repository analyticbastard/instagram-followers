(ns instagram-followers.views.data)

(defn running-section [*is-running]
  (let [is-running? @*is-running]
    (println is-running?)
    [:article.tile.is-child.notification
     [:h1.title "Status"]
     [:div [:span
            (str "Application " (if is-running? "running" "stopped"))]]
     [:form {:action "/start-stop"}
      [:div.buttons.is-centered
       [:p.control
        [:button {:type "submit" :class (str "button is-medium " (if is-running? "is-danger" "is-success"))}
         [:span.icon.is-medium [:i {:class (str "fas " (if is-running? "fa-pause" "fa-play"))}]]
         [:span (if is-running? "Stop" "Start")]]]]]]))

(defn index [*is-running {:keys [users likes]}]
  (let [is-running? @*is-running]
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
        (running-section *is-running)]]
      [:div.tile.is-parent
       [:article.tile.is-child.notification
        [:h1.title "Statistics"]
        [:div [:span users] " users retrieved"]
        [:div [:span likes] " likes given so far"]]]]]))