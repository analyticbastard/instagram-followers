(ns instagram-followers.views.top)

(defn index []
  [:div
   [:div {:class "center"}
    [:h1.title "Welcome"]
    [:form {:action "/login" :method "POST"}
     [:div "Username" [:input {:type "text" :name "username"}]]
     [:div "Password" [:input {:type "password" :name "password"}]]
     [:div [:input {:type "submit" :class "button" :value "Login"}]]]]])
