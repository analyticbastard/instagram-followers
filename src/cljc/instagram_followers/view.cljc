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

(def status (atom {}))

(rum/defcs data-page
  < rum.core/static rum.core/reactive
  [state label]
  (data/index (rum/react status)))

(rum/defc page < rum/reactive [state]
  (data-page (rum/react state)))

(rum/defc layout
  < rum.core/static
  [& body]
  [:html
   (html-headers)
   [:body
    [:div#app
     (header {})
     [:section.section
      [:div.container#page
       body]]]
    (javascripts)]])
