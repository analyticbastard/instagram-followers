(ns instagram-followers.logging
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :refer [Lifecycle]]
            [clojure.tools.logging :as log])
  (:import org.slf4j.LoggerFactory))

(defn set-default-uncaught-exception-handler []
  (Thread/setDefaultUncaughtExceptionHandler
    (reify Thread$UncaughtExceptionHandler
      (uncaughtException [_ thread ex]
        (log/error ex "Uncaught exception on" (.getName thread))))))

(defn set-level! [ns level]
  (let [context (LoggerFactory/getILoggerFactory)
        l (.getLogger context (name ns))
        level (.toUpperCase (name level))]
    (log/info "Setting log level" ns level)
    (.setLevel l (eval (read-string (format "ch.qos.logback.classic.Level/%s" level))))))

(defrecord Logging [loggers]
  Lifecycle

  (start [this]
    #_(doseq [{:keys [ns level]} loggers]
      (set-level! ns level))
    this)

  (stop [this]
    this))