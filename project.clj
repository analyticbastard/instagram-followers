(defproject instagram-followers "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.7.1"
  :main ^:skip-aot instagram-followers.core
  :target-path "target/%s"
  :source-paths ["src/clj" "src/cljc"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/tools.reader "1.3.2"] ;; for :as :clojure
                 [org.clojure/tools.nrepl "0.2.13"]
                 
                 [aero "1.1.3"]

                 [com.stuartsierra/component "0.3.2"]
                 [com.stuartsierra/component.repl "0.2.0"]
                 [org.danielsz/system "0.4.1"]
                 [bonney "0.1.0"]

                 [cheshire "5.8.1"]
                 [clj-http "3.9.1"]
                 [metosin/muuntaja "0.5.0"]

                 [ring "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [com.cemerick/friend "0.2.3"]
                 [bidi "2.1.3"]
                 [yada "1.2.15"]
                 [com.ninjudd/eventual "0.5.0"]

                 [rum "0.11.2"]
                 [garden "1.3.6"]
                 [venantius/accountant "0.2.4"]
                 [cljs-http "0.1.45"]

                 ;; Logging
                 [org.clojure/tools.logging "0.2.6"]
                 [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                 [ch.qos.logback/logback-access "1.2.3"]
                 [ch.qos.logback/logback-core "1.2.3"]
                 [org.slf4j/slf4j-api "1.7.6"]

                 ;; Java > 8
                 [javax.xml.bind/jaxb-api "2.3.1"]
                 ]
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
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
