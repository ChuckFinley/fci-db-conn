(ns fci-db-conn.push-data
	(:use [korma db core])
	(import java.text.SimpleDateFormat))
	
(def sdf (SimpleDateFormat. "yyyy-MM-dd"))

(def dbparams 
	{:host "fcidbinstance.cobtk5viudpw.us-east-1.rds.amazonaws.com"
	 :db   "fcidb"})

(defn db-conn [user password]
	(do
		(println "Connecting to" (:host dbparams))
		(defdb fcidb (mysql (assoc dbparams :user user :password password)))))

(defn date-to-string [date]
	(if (identity date) (.format sdf date)))
	
(defentity Chicks
	(transform (fn [chick]
		(update-in chick [:Hatched :Measure :MaxMeasured] date-to-string))))

(defn clear-data []
	(do
		(println "Deleting existing data")
		(delete Chicks)))

(defn insert-dataset [dataset]
	(do
		(println "Inserting" (count dataset) "rows for" (->> dataset first :Year))
		(insert Chicks (values dataset))))
		
(defn try-insert-row [row]
	(try
		(insert Chicks (values row))
	(catch Exception e
		(println *e))))
		
(defn try-insert-dataset [dataset]
	(do
		(println "Inserting" (count dataset) "rows for" (->> dataset first :Year))
		(map try-insert-row dataset)))