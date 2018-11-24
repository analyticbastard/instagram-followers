(ns instagram-followers.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [clojure.string :as s]))

(defmethod aero/reader 'set
  [_ _ value]
  (when value
    (try
      (set value)
      (catch Exception _
        nil))))

(defmethod aero/reader 'safelong
  [opts tag value]
  (when value (Long/parseLong (str value))))

(defn config [profile]
  (-> "config.edn"
      clojure.java.io/resource
      (aero/read-config {:profile profile})))

(defn configure [system profile]
  (merge-with merge
              system
              (config profile)))