(defproject fci-db-conn "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
				 [dk.ative/docjure "1.6.0"]
				 [korma "0.3.0"]
				 [mysql/mysql-connector-java "5.1.25"]]
  :repl-options {:init-ns fci-db-conn.core})
