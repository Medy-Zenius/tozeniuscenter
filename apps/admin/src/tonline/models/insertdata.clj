(ns tonline.models.insertdata
  (:require [tonline.models.db :as db]
            [tonline.shares.share :as share]))

(defn proset [gol kkat ket jsoal waktu jumpil]
  (let [vjenis (if (= "0" jumpil) "2" "1")
        vjsoal (Integer/parseInt jsoal)]
    (db/insert-data "proset"
                  {:golongan gol
                   :kodekategori (read-string kkat)
                   :keterangan ket
                   :jsoal vjsoal
                   :waktu (Integer/parseInt waktu)
                   :kunci (str (vec (repeat (Integer/parseInt jsoal) "-")))
                   :jenis (str (vec (repeat (Integer/parseInt jsoal) vjenis)))
                   :upto (str (vec (repeat (Integer/parseInt jsoal) jumpil)))
                   :pretext (str (vec (repeat (Integer/parseInt jsoal) "-")))
                   :sound (str (vec (repeat (Integer/parseInt jsoal) "-")))
                   :jumpil (read-string jumpil)
                   :acak "0"
                   :polaacak (str (vec (range 1 (inc vjsoal))))
                   :status "0"
                   :skala 100
                   :nbenar 1
                   :nsalah 0
                   :justonce "0"
                   :kodex (share/create-kode 32)})))

(defn users [nis nama
             ;email
             npsn kelas vcode pass]
  (db/insert-data "users" {:nis nis
                           :name nama
                           ;:email email
                           :npsn npsn
                           :kelas kelas
                           :vcode vcode
                           :pass pass
                           :activation "1"
                           :status 1}))

(defn admin [id status pass]
  (db/insert-data "admin" {:id id
                          :status status
                          :pass pass}))

(defn simsbmptn [ket tkpa jurusan kelompok]
  (db/insert-data "simsbmptn" {:keterangan ket
                               :kodetkpa (read-string tkpa)
                               :kodeipaorips (read-string jurusan)
                               :kelompok kelompok
                                }))

(defn sekolah [npsn nama alamat telpon email]
  (db/insert-data "sekolah" {:npsn npsn :nama nama :alamat alamat
                             :telpon telpon :email email}))

(defn simppdb [ket mat ipa ind ing]
  (db/insert-data "simppdb" {:keterangan ket
                             :kodemat (read-string mat)
                             :kodeipa (read-string ipa)
                             :kodeind (read-string ind)
                              :kodeing (read-string ing)}))

(defn pgsma [kode sek nm]
  (db/insert-data "pgsma" {:kodedaerah (read-string kode)
                           :sekolah sek
                           :nm (read-string nm)}))

(defn kodedaerah [daerah]
  (db/insert-data "kodedaerah" {:daerah daerah}))
