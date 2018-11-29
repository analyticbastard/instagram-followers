(ns dev
  (:require [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.tools.namespace.repl :refer [refresh refresh-all clear]]
            [com.stuartsierra.component.repl :refer [reset set-init start stop system]]
            [figwheel-sidecar.system :refer [create-figwheel-system]]
            [instagram-followers.system :as system]))

;; Do not try to load source code from 'resources' directory
(clojure.tools.namespace.repl/set-refresh-dirs "dev" "src" "test")

(defn dev-system
  "Constructs a system map suitable for interactive development."
  []
  (-> (system/new-system :dev)
      (assoc :figwheel
             (create-figwheel-system
               {:figwheel-options {}
                :build-ids ["dev"]
                :all-builds [{:id "dev"
                              :figwheel true
                              :source-paths ["src/cljs" "src/cljc"]
                              :compiler {:main "instagram-followers.client"
                                         :asset-path "/js/out"
                                         :output-to "resources/public/js/main.js"
                                         :output-dir "resources/public/js/out"
                                         :closure-defines {'goog.DEBUG true}
                                         :verbose false
                                         :optimizations :none}}]}))))

(set-init (fn [_] (dev-system)))

;; bidi non loading on dev, force load
(require '[bidi.bidi])
