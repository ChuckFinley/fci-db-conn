(ns fci-db-conn.core
	(:require [fci-db-conn.pull-data :as pull]
			  [fci-db-conn.push-data :as push]))

(defn go [] (pull/get-data (nth pull/chick-files 25)))