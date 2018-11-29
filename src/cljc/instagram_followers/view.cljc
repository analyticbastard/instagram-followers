(ns instagram-followers.view
  (:require [rum.core :as rum]
    [instagram-followers.views.data :as data]
    #?(:cljs [instagram-followers.flow :refer [dispatch]])))


(defn html-headers []
  [:head
   [:title "Instagram Love"]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   [:link {:rel "shortcut icon" :href "data:image/x-icon;," :type "image/x-icon"}]
   [:link {:rel "stylesheet" :href "https://cdnjs.cloudflare.com/ajax/libs/bulma/0.6.2/css/bulma.min.css"}]
   [:link {:rel "stylesheet" :href "styles.css"}]])

(defn javascripts []
  [:div
   [:script {:src "https://use.fontawesome.com/releases/v5.0.8/js/all.js"
             :defer true
             :integrity "sha384-SlE991lGASHoBfWbelyBPLsUlwY1GwNDJo3jSJO04KZ33K2bwfV9YBauFfnzvynJ"
             :crossorigin "anonymous"}]
   [:script {:src "/js/main.js"}]])

(rum/defc header [{:keys [cart-item-num]}]
  [:nav.navbar.is-light {:role "navigation" :aria-label "main navigation"}
   [:div.navbar-brand
    [:a.navbar-item {:href "/"}
     ]
    [:div.navbar-burger
     [:span] [:span] [:span]]]])

(rum/defc current-view < rum/reactive [state]
  [:div
   [:p "View:" (:view state)]
   [:div {:on-click #?(:cljs #(dispatch :sample 1)
                       :clj nil)
          } "Sample:" (rum/react (:sample state))]])

(rum/defc form [state]
  [:.app
   [:h1 (str "Hello, " (:name state))]
   [:input {:type      "input"
            :value     (or (:name state) "")
            :on-change #?(:clj nil :cljs #(dispatch :change-name (.. % -target -value)))}]
   [:h1 (str "View keyword: " (:view-kw state))]
   [:input {:type      "input"
            :value     (or (:url state) "")
            :on-change #?(:clj nil :cljs #(dispatch :change-url (.. % -target -value)))}]])

(rum/defc data [state]
  (data/index true {:users 0 :likes 1}))

(rum/defc data-running-section [state]
  (data/running-section true))

#_(rum/defc app < rum/reactive [state]
  [:div
   (header {})
   [:.section
    [:.container
     (data (rum/react state))]]])

(rum/defc running-section < rum/reactive [state]
  (data-running-section (rum/react state)))

(defn layout [& body]
  [:html
   (html-headers)
   [:body
    [:div#app
     (header {})
     [:section.section
      [:div.container
       body]]]
    (javascripts)]])
