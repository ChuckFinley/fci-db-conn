(ns fci-db-conn.core
	(:require [fci-db-conn.pull-data :as pull]
			  [fci-db-conn.push-data :as push]))

(defn go [username password]
	(let [datasets (map pull/get-data pull/chick-files)]
		(push/db-conn username password)
		(push/clear-data)
		(map push/insert-dataset datasets)))

(defn resume [username password start]
	(let [datasets (map pull/get-data (subvec (vec pull/chick-files) start))]
		(push/db-conn username password)
		(map push/insert-dataset datasets)))
		
(defn try-go [username password]
	(let [datasets (map pull/get-data pull/chick-files)]
		(map push/try-insert-dataset datasets)))
		