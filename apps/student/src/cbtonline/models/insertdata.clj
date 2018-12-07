(ns cbtonline.models.insertdata
  (:require [cbtonline.models.db :as db]
            [cbtonline.shares.share :as share]))

(defn user
  [npsn nis nama email vpass vcode]
  (db/insert-data "users" {:npsn npsn :name nama :email email :nis nis
                                 :pass vpass :status 1
                                 :activation "0" :vcode vcode}))

(defn datato [nis kode jawaban nilai tanggal]
  (db/insert-data "datato"
                           {:nis nis
                            :kode kode
                            :jawaban jawaban
                            :nilai nilai
                            :tanggal tanggal}))
