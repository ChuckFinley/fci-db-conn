(ns fci-db-conn.core
	(:require [dk.ative.docjure.spreadsheet :as xl])
	(:import (org.apache.poi.ss.usermodel Cell)))

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

(defn get-data [file]
	(->> (xl/load-workbook file)
		 (xl/select-sheet "Sheet1")
		 (xl/select-columns raw-data-cols)))
		 
;(get-data (last chick-files))