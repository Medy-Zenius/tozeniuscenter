(ns tonline.models.deletedata
  (:require [tonline.models.db :as db]))

(defn users [nis]
    (db/delete-data "users" (str "nis='" nis "'")))

(defn simsbmptn [kode]
  (db/delete-data "simsbmptn" (str "kode='" kode "'")))

(defn simppdb [kode]
  (db/delete-data "simppdb" (str "kode='" kode "'")))

(defn pgsma [nomer]
  (db/delete-data "pgsma" (str "nomer='" nomer "'")))


