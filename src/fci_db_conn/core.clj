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
	 
(defn pipe [value & fns]
	(reduce (fn [acc cur] (cur acc)) value fns))

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
	
(defn fn-mapper [k f]
	(fn [dataset] (map (fn [row] (assoc row k (f row dataset))) dataset)))
	 
(defn get-data [file]
	(let [raw-data (->> (xl/load-workbook file)
						 (xl/select-sheet "Sheet1")
						 (xl/select-columns raw-data-cols)
						 (drop 1))]
		(pipe raw-data
			(fn-mapper :position get-position)
			(fn-mapper :year get-year)
			(fn-mapper :chick-id get-chick-id))))
		 
;(get-data (last chick-files))