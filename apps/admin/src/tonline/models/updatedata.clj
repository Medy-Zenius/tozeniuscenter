(ns tonline.models.updatedata
  (:require [tonline.models.db :as db]
            [clojure.string :as st]
            ;[tonline.shares.share :as share]
            ))

(defn kunci [kode jenis upto kunci]
  (db/update-data-1 "proset" ["kode=?" (Integer/parseInt kode)]
                    {:kunci (str (st/split kunci #":"))
                     :jenis (str (st/split jenis #","))
                     :upto (str (st/split upto #","))}))

(defn kunci-only [kode kunci]
  (db/update-data-1 "proset" ["kode=?" (Integer/parseInt kode)]
                    {:kunci kunci}))

(defn set-soal [kode gol ket vjsoal waktu jumpil newkunci newjenis newupto skala nbenar nsalah
                newpretext newsound newpolaacak]
  (db/update-data-1 "proset" ["kode=?" (Integer/parseInt kode)]
                      {:golongan gol
                       :keterangan ket
                       :jsoal vjsoal
                       :waktu (Integer/parseInt waktu)
                       :jumpil (Integer/parseInt jumpil)
                       :kunci newkunci
                       :jenis newjenis
                       :upto newupto
                       :skala (Integer/parseInt skala)
                       :nbenar (Integer/parseInt nbenar)
                       :nsalah (Integer/parseInt nsalah)
                       :pretext newpretext
                       :sound newsound
                       :polaacak newpolaacak
                       }))

(defn acak-proset [kode acak polaacak]
  (db/update-data-1 "proset" ["kode=?" (Integer/parseInt kode)]
                    {:acak acak
                     :polaacak polaacak}))

(defn aktif-proset [kode status]
  (db/update-data-1 "proset" ["kode=?" (Integer/parseInt kode)]
                    {:status status}))

(defn save-kunci-image [kunci jenis upto pretext sound kode]
  (db/update-data-1 "proset" ["kode=?" (Integer/parseInt kode)]
                  {:kunci (str (st/split kunci #":"))
                   :jenis (str (st/split jenis #","))
                   :upto (str (st/split upto #","))
                   :pretext (str (st/split pretext #":"))
                   :sound (str (st/split sound #":"))}))

(defn constant [nama value]
  (db/update-data-1 "constants" ["nama=?" nama]
                    {:value value}))

(defn justonce [kode justonce]
  (db/update-data-1 "proset" ["kode=?" (Integer/parseInt kode)]
                    {:justonce justonce}))

(defn users [nislama nisbaru nama kelas email npsn]
  (db/update-data-1 "users" ["nis=?" nislama]
                            {:nis nisbaru
                             :name nama
                             :kelas kelas
                             :email email
                             :npsn npsn}))

(defn password [pwbaru id]
  (db/update-data-1 "admin" ["id=?" id]
                    {:pass pwbaru}))

(defn simsbmptn [kode ket tkpa ipaorips kelompok]
  (db/update-data-1 "simsbmptn"
                              ["kode=?" (read-string kode)]
                                      {:keterangan ket
                                       :kodetkpa (read-string tkpa)
                                       :kodeipaorips (read-string ipaorips)
                                       :kelompok kelompok}))

(defn sekolah [npsn nama alamat telpon email nomer]
  (db/update-data-1 "sekolah"
                              ["nomer=?" (read-string nomer)]
                                      {:npsn npsn
                                       :nama nama
                                       :alamat alamat
                                       :telpon telpon
                                       :email email}))
(defn simppdb [kode ket mat ipa ind ing]
(db/update-data-1 "simppdb"
                              ["kode=?" (read-string kode)]
                                      {:keterangan ket
                                       :kodemat (read-string mat)
                                       :kodeipa (read-string ipa)
                                       :kodeind (read-string ind)
                                       :kodeing (read-string ing)}))
(defn pgsma [nomer sekolah kode nm]
(db/update-data-1 "pgsma" ["nomer=?" (read-string nomer)]
                          {:sekolah sekolah
                           :kodedaerah (read-string kode)
                           :nm (read-string nm)}))

(defn kodedaerah [kode daerah]
  (db/update-data-1 "kodedaerah"
                                ["kode=?" (read-string kode)]
                                        {:daerah daerah}))

(defn nilai [kode nis nilai]
  (db/update-data "datato"
                  ;["kode=?" (read-string kode) and "nis=?" nis]
                                     (str "kode='" kode "' and nis='" nis "'")
                            {:nilai nilai}))
