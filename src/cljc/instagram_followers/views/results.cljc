(ns instagram-followers.views.results)

(def form
  [:form {:action "/data"}
   [:button {:type "submit"} "Back"]])

(defn ok []
  [:div
   [:h1 "OK"]
   form])

(defn fail []
  [:div
   [:h1 "FAIL"]
   form])
