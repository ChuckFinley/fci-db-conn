(ns fci-db-conn.pull-data
	(:require [dk.ative.docjure.spreadsheet :as xl])
	(:import (org.apache.poi.ss.usermodel Cell)
			 (java util.Date util.concurrent.TimeUnit text.SimpleDateFormat)))

; Handle error cells when reading columns
(defmethod xl/read-cell Cell/CELL_TYPE_ERROR [^Cell cell] nil)

(def chick-dir (clojure.java.io/file "C:\\Users\\HADDOCK\\Downloads\\Final Chick Files\\Final Chick Files"))
(def chick-files (filter #(.contains % "\\April 2013 recovery") (map str (file-seq chick-dir))))

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
	 :Position
	 :ChickId
	 :Age
	 :MaxMeasured
	 :MaxAge
	 :PeakWeight
	 :FledgeWeight
	 :PeakWing
	 :MeasurementId])
	
(def sdf (SimpleDateFormat. "yyyy-MM-dd"))
	 
(defn chick-data [row dataset]
	(filter #(= (:ChickId row) (:ChickId %)) dataset))

(defn get-Year [row dataset]
	(+ 1900 (.getYear (:Measured row))))
	
(defmulti get-Position (fn [row dataset] (type (:Position row))))

(defmethod get-Position java.lang.Double [row dataset]
	(int (:Position row)))

(defmethod get-Position java.lang.String [row dataset]
	(:Position row))

(defn get-ChickId [row dataset]
	(str (:Year row) "_" (:Nest row) "-" (:Position row)))
	
(defn get-Age [row dataset]
	(letfn [(date-diff [d1 d2] 
		(.convert TimeUnit/DAYS (- (.getTime d2) (.getTime d1)) TimeUnit/MILLISECONDS))]
		(if-let [hatched (:Hatched row)]
			(+ 1 (date-diff hatched (:Measured row))))))
			
(defn get-MaxMeasured [row dataset]
	(let [dates (map :Measured (chick-data row dataset))]
		(last dates)))
		
(defn get-MaxAge [row dataset]
	(let [ages (map :Age (chick-data row dataset))]
		(last ages)))
		
(defn get-PeakWeight [row dataset]
	(if-let [weights (seq (filter identity (map :Weight (chick-data row dataset))))]
		(apply max weights)))
		
(defn get-FledgeWeight [row dataset]
	(if-let [weights (seq (filter identity (map :Weight (chick-data row dataset))))]
		(if (= (:Fate row) "Fledge") (last weights))))
		
(defn get-PeakWing [row dataset]
	(if-let [wings (seq (filter identity (map :Wing (chick-data row dataset))))]
		(apply max wings)))
		
(defn get-MeasurementId [row dataset]
	(str (.format sdf (:Measured row)) "_" (:ChickId row)))

(defn getter [field]
	(->> field name (str "get-") symbol (ns-resolve 'fci-db-conn.pull-data)))
	
(defn calc-derived-field [dataset field]
	(map (fn [row] (assoc row field ((getter field) row dataset))) dataset))
	
(defn calc-all-derived-fields [dataset fields]
	(reduce calc-derived-field dataset fields))
	 
(defn get-data [file]
	(let [raw-data (->> (xl/load-workbook file)
						(xl/select-sheet "Sheet1")
						(xl/select-columns raw-data-cols)
						(drop 1))]
		(calc-all-derived-fields raw-data derived-fields)))
