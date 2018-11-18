(ns instagram-followers.view
  (:require [rum.core :as rum]))


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
   #_[:script {:src "/js/main.js"}]])

(rum/defc header [{:keys [cart-item-num]}]
  [:nav.navbar.is-light {:role "navigation" :aria-label "main navigation"}
   [:div.navbar-brand
    [:a.navbar-item {:href "/"}
     ]
    [:div.navbar-burger
     [:span] [:span] [:span]]]])

(rum/defc price [n]
  [:span [:span {:dangerouslySetInnerHTML {:__html "&yen;"}}] [:span n]])

(rum/defc current-view < rum/reactive [state]
  [:div
   [:p "View:" (:view state)]
   [:div {:on-click (:clj nil) ;:cljs #(dispatch :sample 1)
          } "Sample:" (rum/react (:sample state))]])

(rum/defc form [state]
  [:.app
   [:h1 (str "Hello, " (:name state))]
   [:input {:type      "input"
            :value     (or (:name state) "")
            :on-change nil}] ;
   [:h1 (str "View keyword: " (:view-kw state))]
   [:input {:type      "input"
            :value     (or (:url state) "")
            :on-change nil}]])

(rum/defc app < rum/reactive [state]
  [:div
   (header {})
   [:.section
    [:.container
     (form (rum/react state))]]])

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
