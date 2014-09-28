(ns fci-db-conn.push-data
	(:use [korma db core])
	(import java.text.SimpleDateFormat))
	
(def sdf (SimpleDateFormat. "yyyy-MM-dd"))

(def dbparams 
	{:host "fcidbinstance.cobtk5viudpw.us-east-1.rds.amazonaws.com"
	 :db   "fcidb"})

(defn db-conn [user password]
	(defdb fcidb (mysql(assoc dbparams :user user :password password))))

(defn date-to-string [date]
	(if (identity date) (.format sdf date)))
	
(defentity Chicks
	(transform (fn [{:keys [Hatched Measured MaxMeasured] :as chick}]
		(assoc chick 
			:Hatched (date-to-string Hatched)
			:Measured (date-to-string Measured)
			:MaxMeasured (date-to-string MaxMeasured)))))

(defn insert-dataset [dataset]
	(insert Chicks (values dataset)))