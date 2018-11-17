(defproject instagram-followers "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main ^:skip-aot instagram-followers.core
  :target-path "target/%s"
  :source-paths ["src/clj" "src/cljc" "dev"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/tools.reader "1.3.2"] ;; for :as :clojure
                 
                 [aero "1.1.3"]

                 [com.stuartsierra/component "0.3.2"]
                 [org.danielsz/system "0.4.1"]
                 [bonney "0.1.0"]

                 [cheshire "5.8.1"]
                 [clj-http "3.9.1"]

                 [ring "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [rum "0.11.2"]
                 [bidi "2.1.3"]
                 [metosin/muuntaja "0.5.0"]
                 [venantius/accountant "0.2.4"]

                 ;; Logging
                 [org.clojure/tools.logging "0.2.6"]
                 [ch.qos.logback/logback-classic "1.1.2" :exclusions [org.slf4j/slf4j-api]]
                 [ch.qos.logback/logback-access "1.1.2"]
                 [ch.qos.logback/logback-core "1.1.2"]
                 [org.slf4j/slf4j-api "1.7.6"]
                 [javax.xml.bind/jaxb-api "2.3.1"]
                 ]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [com.stuartsierra/component.repl "0.2.0"]
                                  [figwheel-sidecar "0.5.17"]]}}
  :repl-options {:timeout 250000
                 :init-ns user}
  :javac-options ["--add-modules java.xml.bind"]
  :cljsbuild {:builds [{:source-paths ["src/cljs" "src/cljc"]
                        :compiler {:main "front.web.client"
                                   :asset-path "/js/out"
                                   :closure-defines {"goog.DEBUG" false}
                                   :verbose true
                                   :output-to "resources/public/js/main.js"
                                   :optimizations :advanced
                                   :pretty-print false}}]})
