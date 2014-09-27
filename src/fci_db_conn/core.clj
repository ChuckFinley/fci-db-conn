(ns fci-db-conn.core
	(:require [dk.ative.docjure.spreadsheet :as xl])
	(:import (org.apache.poi.ss.usermodel Cell)
			 (java.util.Date)))

; Handle error cells when reading columns
(defmethod xl/read-cell Cell/CELL_TYPE_ERROR [^Cell cell] nil)

(def chick-dir (clojure.java.io/file "C:\\Users\\HADDOCK\\Downloads\\Final Chick Files\\Final Chick Files"))
(def chick-files (filter #(.contains % "\\April 2013 recovery") (map str (file-seq chick-dir))))

(def raw-data-cols
	{:D :type
	 :E :old-nest
	 :F :nest
	 :G :position
	 :H :hatched
	 :I :measured
	 :K :wing
	 :L :weight
	 :P :end-cndtn
	 :Q :fate
	 :R :fate-cause
	 :S :day-cndtn
	 :T :band-no
	 :U :cohort-no
	 :V :comments
	 :W :questions})
	 
(def derived-fields
	[:year
	 :position
	 :chick-id
	 :age])

(defn get-year [row dataset]
	(+ 1900 (.getYear (:measured row))))
	
(defn get-position [row dataset]
	(let [pos (:position row)]
		(case pos
			java.lang.String pos
			java.lang.Double (int pos)
			(type pos))))

(defn get-chick-id [row dataset]
	(str (:year row) "_" (:nest row) "-" (:position row)))
	
(defn get-age [row dataset]
	(if-let [hatched (:hatched row)]
		(date-diff hatched (:measured row))))

(defn getter [field]
	(->> field name (str "get-") symbol resolve))
	
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
		 
;(get-data (last chick-files))