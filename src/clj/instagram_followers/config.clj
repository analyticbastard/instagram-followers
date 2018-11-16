(ns instagram-followers.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [clojure.string :as s]))

(defmethod aero/reader 'split
  [_ _ value]
  (when value
    (try
      (s/split value #",")
      (catch Exception _
        nil))))

(defmethod aero/reader 'set
  [_ _ value]
  (when value
    (try
      (set value)
      (catch Exception _
        nil))))

(defmethod aero/reader 'map-keyword
  [_ _ value]
  (when value
    (try
      (map keyword value)
      (catch Exception _
        nil))))

(defmethod aero/reader 'map-long
  [_ _ value]
  (when value
    (try
      (map #(aero/reader nil 'long %) value)
      (catch Exception _
        nil))))

(defn config [profile]
  (-> "config.edn"
      clojure.java.io/resource
      (aero/read-config {:profile profile})))

(defn configure [system profile]
  (merge-with merge
              system
              (config profile)))