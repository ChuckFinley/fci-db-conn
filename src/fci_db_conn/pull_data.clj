(ns fci-db-conn.pull-data
	(:require [dk.ative.docjure.spreadsheet :as xl])
	(:import (org.apache.poi.ss.usermodel Cell)
			 (java util.Date util.concurrent.TimeUnit text.SimpleDateFormat)))

; Handle error cells when reading columns
(defmethod xl/read-cell Cell/CELL_TYPE_ERROR [^Cell cell] nil)

(def chick-dir (clojure.java.io/file "C:\\Users\\HADDOCK\\Downloads\\Final Chick Files\\Final Chick Files"))
(def chick-files (vec (filter #(.contains % "\\April 2013 recovery") (map str (file-seq chick-dir)))))

(def raw-data-cols
	{:D :Type
	 :E :OldNest
	 :F :Nest
	 :G :Position
	 :H :Hatched
	 :I :Measured
	 :K :Wing
	 :L :Weight
	 :P :EndCndtn
	 :Q :Fate
	 :R :FateCause
	 :S :DayCndtn
	 :T :BandNo
	 :U :CohortNo
	 :V :Comments
	 :W :Questions})
	 
(def derived-fields
	[:Year
	 :ChickId
	 :Age
	 :MaxMeasured
	 :MaxAge
	 :PeakWeight
	 :FledgeWeight
	 :PeakWing
	 :MeasurementId])
	 
(def field-conversions
	(letfn [(str-to-int [v] (if (number? v) (int v) v))]
		{:Type		 str
		 :OldNest	 str-to-int
		 :Nest		 str
		 :Position	 str-to-int
		 :Hatched	 #(cast java.util.Date %)
		 :Measured	 #(cast java.util.Date %)
		 :Wing		 double
		 :Weight	 double
		 :EndCndtn	 str
		 :Fate		 str
		 :FateCause	 str
		 :DayCndtn	 str
		 :BandNo	 str
		 :CohortNo	 str
		 :Comments	 str
		 :Questions	 str}))
	
(def sdf (SimpleDateFormat. "yyyy-MM-dd"))

(defn date? [d] (= java.util.Date (type d)))
	 
(defn chick-data [row dataset]
	(filter #(= (:ChickId row) (:ChickId %)) dataset))

(defn get-Year [row dataset]
	(try
	(+ 1900 (.getYear (:Measured row)))
	(catch Exception e (println "year" row))))

(defn get-ChickId [row dataset]
	(str (:Year row) "_" (:Nest row) "_" (:Position row)))
	
(defn get-Age [row dataset]
	(try
	(if (date? (:Hatched row))
		(letfn [(date-diff [d1 d2] 
			(.convert TimeUnit/DAYS (- (.getTime d2) (.getTime d1)) TimeUnit/MILLISECONDS))]
			(if-let [hatched (:Hatched row)]
				(+ 1 (date-diff hatched (:Measured row))))))
	(catch Exception e (println "age" row))))
			
(defn get-MaxMeasured [row dataset]
	(let [dates (map :Measured (chick-data row dataset))]
		(last dates)))
		
(defn get-MaxAge [row dataset]
	(let [ages (map :Age (chick-data row dataset))]
		(last ages)))
		
(defn get-PeakWeight [row dataset]
	(try
	(if-let [weights (seq (filter identity (map :Weight (chick-data row dataset))))]
		(apply max weights))
	(catch Exception e (println "weight" row))))
		
(defn get-FledgeWeight [row dataset]
	(if-let [weights (seq (filter identity (map :Weight (chick-data row dataset))))]
		(if (= (:Fate row) "Fledge") (last weights))))
		
(defn get-PeakWing [row dataset]
	(try
	(if-let [wings (seq (filter identity (map :Wing (chick-data row dataset))))]
		(apply max wings))
	(catch Exception e (println "wing" row))))
		
(defn get-MeasurementId [row dataset]
	(str (.format sdf (:Measured row)) "_" (:ChickId row)))

(defn getter [field]
	(->> field name (str "get-") symbol (ns-resolve 'fci-db-conn.pull-data)))
	
(defn calc-derived-field [dataset field]
	(map (fn [row] (assoc row field ((getter field) row dataset))) dataset))
	
(defn calc-all-derived-fields [dataset fields]
	(reduce #(do (println \tab (name %2)) (calc-derived-field %1 %2)) dataset fields))

(defn validate-row [row]
	(into {} (map (fn [[k v]] [k (try ((k field-conversions) v) (catch Exception e (println row k v e)))]) row)))
	
(defn get-data [file]
	(do
		(print "Importing data... ")
		(let [raw-data (->> (xl/load-workbook file)
							(xl/select-sheet "Sheet1")
							(xl/select-columns raw-data-cols)
							(drop 1)
							(filter identity)
							(map validate-row))]
			(println (count raw-data) "rows in" (get-Year (first raw-data) raw-data))
			(println "Calculating derived fields...")
			(calc-all-derived-fields raw-data derived-fields))))
