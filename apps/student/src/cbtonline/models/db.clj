(ns cbtonline.models.db
    (:require [clojure.java.jdbc :as sql]))

(def db
{:subprotocol "postgresql"
:subname "//wf-103-44-220-83.webfaction.com:5432/tonline"
:user "tonline"
:password "tonline2000"})

(defmacro with-db [f & body]
`(sql/with-connection ~db (~f ~@body)))

(defn delete-user [userid]
(with-db sql/delete-rows :users ["id=?" userid]))

(defn get-data [query toggle]
  (if (= toggle 1)
  (with-db sql/with-query-results res [query] (first res))
  (with-db sql/with-query-results res [query] (doall res))))

(defn insert-data [table data]
   (with-db sql/insert-record (keyword table) data))

(defn update-data [table query data]
  (with-db sql/update-values (keyword table) [query] data))

(defn update-data-1
  "table: table name
  query: where-params
  data: column to update"
  [table query data]
  (with-db sql/update-values (keyword table) query data))

(defn delete-data [table query]
(with-db sql/delete-rows (keyword table) [query]))

