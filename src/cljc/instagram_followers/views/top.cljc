(ns instagram-followers.views.top)

(defn index []
  [:div
   [:h1.title "Welcome"]
   [:div
    [:form {:action "/login"}
     [:input {:type "password"}]]]])
