(ns cbtonline.models.updatedata
  (:require [cbtonline.models.db :as db]
            [clojure.string :as st]
            ;[cbtonline.shares.share :as share]
            ))

(defn kunci [kode jenis upto kunci]
  (db/update-data-1 "proset" ["kode=?" (Integer/parseInt kode)]
                    {:kunci (str (st/split kunci #":"))
                     :jenis (str (st/split jenis #","))
                     :upto (str (st/split upto #","))}))

(defn user-activation [nis]
  (db/update-data-1 "users" ["nis=?" nis] {:activation "1"}))

(defn password [pass user]
  (db/update-data-1 "users" ["nis=?" user] {:pass pass}))

(defn pwlupa [email vcode pass]
  (db/update-data-1 "users" ["email=? AND vcode=?" email vcode]
                    {:pass pass}))

(defn datato [nis kode jawaban nilai tanggal]
  (db/update-data-1 "datato" ["nis=? AND kode=?" nis kode]
                           {:nis nis
                            :kode kode
                            :jawaban jawaban
                            :nilai nilai
                            :tanggal tanggal}))

(defn email [nis email]
  (db/update-data-1 "users" ["nis=?" nis] {:email email}))
