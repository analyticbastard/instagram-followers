(ns instagram-followers.views.styles
  (:require [garden.selectors :refer [after]]))

(defn styles []
  [[:input {:display "block" :margin-bottom "10px" :width "100%"}]
   [:textarea {:display "block" :margin-bottom "10px" :width "100%"}]
   [".row:after" {:content "" :display "table" :clear "both"}]
   [:.column {:float "left" :width "50%"}]
   ])