(ns instagram-followers.views.data)

(defn index [is-running?]
  [:div.row
   [:h1.title "Data"]
   [:div.column
    [:form {:action "/post" :method "get"}
     [:input {:name "one" :type "login"}]
     [:textarea {:name "two"}]
     [:button {:type "submit"} "Submit"]]]
   [:div.column
    [:h2 "Status"]
    [:span
     (if is-running? "Yes" "No")]
    [:form {:action "/start-stop"}
     [:button {:type "submit"} (if is-running? "Stop" "Start")]]]])