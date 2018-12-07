(ns tonline.controllers.chome
  (:require [compojure.core :refer :all]
            [clojure.string :as st]
            [tonline.views.layout :as layout]
            [tonline.models.getdata :as getdata]
            [tonline.models.insertdata :as insertdata]
            [tonline.models.updatedata :as updatedata]
            [tonline.models.deletedata :as deletedata]
            [tonline.shares.share :as share]
            [noir.session :as session]
            [noir.io :as io]
            [noir.util.crypt :as crypt]
            [noir.response :as resp]
            [dk.ative.docjure.spreadsheet :refer :all]
            [ring.util.response :refer [file-response]]
            [clojure.data.json :as json])
  (:import (java.io File)))

(defn login [id pass]
  (let [user (getdata/admin id)]
    (if user
      (if (= pass (user :pass))
          (do
            (session/put! :id id)
            (session/put! :status (user :status))
            (layout/render "home.html"))
          (layout/render "home.html" {:id id :error "Password Salah!"}))
      (layout/render "home.html" {:error "Tidak ada ID tersebut!"}))))

(defn logout []
  (do
   (session/clear!)
   (resp/redirect "/")))

(defn ganti-pw []
    (layout/render "ganti-pw.html"))

(defn ganti-pw-user [pwlama pwbaru1 pwbaru2]
  (let [pwnow (:pass (getdata/admin (session/get :id)))]
    (if (or (not (= pwnow pwlama)) (< (count pwbaru1) 5))
        (layout/render "ganti-pw.html" {:error "Password Lama tidak benar atau password baru kurang dari lima huruf!"})
        (if (= pwbaru1 pwbaru2)
          (try (updatedata/password pwbaru1 (session/get :id))
                 (do
                    (session/clear!)
                    (resp/redirect "/"))
               (catch Exception ex
                  (layout/render "ganti-pw.html" {:error "Gagal ganti password!"})))
          (layout/render "ganti-pw.html" {:error "Password tidak sesuai!"})))))

(defn home []
  (layout/render "home.html"))

(defn set-constant [nama judul label]
  (let [value (:value (getdata/constant nama))]
    (layout/render "edit-constant.html" {:nama nama
                                         :value value
                                         :judul judul
                                         :label label})))
(defn update-constant [nama value judul]
  (try
    (updatedata/constant nama value)
    (layout/render "pesan.html" {:pesan (str judul " berhasil diubah menjadi " value)})
    (catch Exception ex
      (layout/render "pesan.html" {:pesan (str "Gagal mengubah " judul "! error:" ex)}))))

(defn proset [nset folder nomer]
  (layout/render "proset.html" {:nset nset :folder folder :nomer nomer}))

(defn daftarkan-proset []
  (let [data (getdata/kategori)]
  (layout/render "daftarkan-proset.html" {:data data})))

(defn simpan-proset [gol kkat ket jsoal waktu jumpil]
  (try
    (insertdata/proset gol kkat ket jsoal waktu jumpil)
    (layout/render "pesan.html" {:pesan (str "Berhasil daftarkan proset!")})
      (catch Exception ex
                  (layout/render "pesan.html" {:pesan (str "Gagal daftarkan proset! error: " ex)}))))

(defn search-proset [act]
  (let [data (getdata/kategori)]
    (layout/render "search-proset.html" {:data data :action act})))

(defn handle-search-proset [kode ket act]
  (let [Uket (clojure.string/upper-case ket)
        namakat (getdata/nama-kat kode)
        data (getdata/kategori-proset kode Uket)]
    (layout/render "list-proset.html" {:data data :kkode kode
                                       :ket ket :namakat namakat
                                       :action act})))

(defn edit-kunci [kode kkode]
  (let [datum (getdata/proset-edit kode)
        namakat (getdata/nama-kat kkode)
        page (cond
               (= (datum :golongan) "1") "edit-kunci-image.html"
               (= (datum :golongan) "2") "edit-kunci-image.html"
               :else "edit-kunci-zenpres.html")]
    ;(println kkode)

    (layout/render page {:kunci (read-string (datum :kunci))
                         :jsoal (datum :jsoal)
                         :jumpil (datum :jumpil)
                         :jenis (read-string (datum :jenis))
                         :upto (read-string (datum :upto))
                         :kode kode
                         :namakat namakat
                         :ket (datum :keterangan)
                         :pretext (if (datum :pretext) (read-string (datum :pretext)) nil)
                         :sound (if (datum :sound) (read-string (datum :sound)) nil)
                         })))

(defn save-kunci [kode jenis upto kunci]
  (try
    (updatedata/kunci kode jenis upto kunci)
     (layout/render "pesan.html" {:pesan "Kunci berhasil disimpan!"})
     (catch Exception ex
       (layout/render "pesan.html" {:pesan (str "Gagal simpan kunci! Error:" ex)}))))

(defn save-kunci-image [kunci jenis upto pretext sound kode]
  ;(println kunci)
  (try
    (updatedata/save-kunci-image kunci jenis upto pretext sound kode)
    (layout/render "pesan.html" {:pesan "Kunci berhasil disimpan!"})
    (catch Exception ex
        (layout/render "pesan.html" {:pesan (str "Gagal simpan kunci! error: " ex)}))))

(defn create-directory [kode kkode act]
  (let [namakat (getdata/nama-kat kkode)
        datum (getdata/status-kodex kode)
        status (datum :status)
        kodex (datum :kodex)
        ;predir "soal/"
        predir "resources/public/proset/"
        ]
    (if (= status "0")
      (do
        (io/create-path (str predir kkode "/" kodex) true)
        (layout/render "upload.html" {:kode kode :kkode kkode
                                      :ket (datum :keterangan) :namakat namakat
                                      :action act
                                      :subjek "File"}))
      (layout/render "pesan.html" {:pesan "Status Nol-kan dulu sebelum upload files!"}))))

(defn upload-file [file kode kkode]
  (let [datum (getdata/status-kodex kode)
        kodex (datum :kodex)
        ;predir "soal/"
        predir "resources/public/proset/"
        ]
    (try
      (if (vector? file)
        (doseq [i file]
            (io/upload-file (str predir kkode "/" kodex) i))
        (io/upload-file (str predir kkode "/" kodex) file))
        (layout/render "pesan.html" {:pesan "Berhasil upload file!"})
       (catch Exception ex
                    (layout/render "pesan.html" {:pesan (str "Gagal upload file! error: " ex)}))
      )))

(defn import-create-directory [kode kkode act]
  (let [namakat (getdata/nama-kat kkode)
        datum (getdata/status-kodex kode)
        status (datum :status)
        kodex (datum :kodex)
        ;predir "soal/"
        jsoal (datum :jsoal)
        predir "resources/public/proset/"
        ]
    (if (= status "0")
      (do
        (io/create-path (str predir kkode "/" kodex) true)
        (layout/render "import-zenpres.html"
                                     {:kode kode :kkode kkode :kodex kodex
                                      :ket (datum :keterangan) :namakat namakat
                                      :jsoal jsoal
                                      :no 1}))
      (layout/render "pesan.html" {:pesan "Status Nol-kan dulu sebelum upload files!"}))))

(defn handle-import-soal-zp [kode kodex kkode namakat ket jsoal no soal]
  (let [msoal (json/read-str soal :key-fn keyword)
        img-ad (:value (getdata/constant "ipnumber"))
        pertanyaan (:text (:soal msoal))
        pertanyaan1 (st/replace pertanyaan #"\n" "")
        perta2 (st/replace pertanyaan1 #"../image/" (str img-ad "/images/"))
        perta3 (st/replace perta2 #"/image/" (str img-ad "/images/"))
        jawaban (:answer msoal)
        pilihan (:choices (:soal msoal))
        spil1 (loop [s "" i 0]
               (if (= i (count pilihan))
                 s
                 (recur (str s "<li>" (second (nth pilihan i)) "</li>") (inc i))))
        spil2 (str "<ol type='A'>" spil1 "</ol>" )
        html (str perta3 spil2)
        nomer (read-string no)
        kunci (read-string (:kunci (getdata/kunci kode)))
        kunci1 (str (vec (concat (take (dec nomer) kunci) [jawaban] (drop nomer kunci))))
        ]
    (do
      ;(println kunci1)
      (spit (str "resources/public/proset/" kkode "/" kodex "/" nomer ".html") html)
      (updatedata/kunci-only kode kunci1)
      (layout/render "import-zenpres.html" {:kode kode :kkode kkode :kodex kodex
                                            :ket ket :namakat namakat
                                            :jsoal (read-string jsoal)
                                            :no (inc nomer)}))))


(defn create-directory-gambar [kode kkode act]
  (let [namakat (getdata/nama-kat kkode)
        datum (getdata/status-kodex kode)]
      (do
        (io/create-path (str "resources/public/images/" kkode "/" kode) true)
        (layout/render "upload.html" {:kode kode :kkode kkode
                                             :ket (datum :keterangan) :namakat namakat
                                             :action act
                                             :subjek "Gambar"}))))

(defn take-gambar []
  (layout/render "upload-fpHTML.html"))

(defn upload-gambar [file]
    (try
      (if (vector? file)
        (doseq [i file]
            (io/upload-file (str "images/") i))
        (io/upload-file (str "images/") file))
        (layout/render "pesan.html" {:pesan "Berhasil upload gambar!"})
       (catch Exception ex
                    (layout/render "pesan.html" {:pesan (str "Gagal upload gambar! error: " ex)}))
      ))

(defn edit-set-soal [kode kkode]
  (let [namakat (getdata/nama-kat kkode)
        datum (getdata/proset-all kode)]
    (layout/render "edit-set-soal.html" {:datum datum :namakat namakat :kkode kkode})))

(defn update-set-soal [kode gol ket jsoal waktu jumpil skala nbenar nsalah]
  (let [datum (getdata/proset-all kode)
        oldkunci (read-string (datum :kunci))
        oldjenis (read-string (datum :jenis))
        oldupto (read-string (datum :upto))
        oldpretext (if (datum :pretext) (read-string (datum :pretext)) nil)
        oldsound (if (datum :sound) (read-string (datum :sound)) nil)
        cok (count oldkunci)
        vjsoal (Integer/parseInt jsoal)
        newkunci (cond
                   (= vjsoal cok) (str oldkunci)
                   (< vjsoal cok) (str (vec (take vjsoal oldkunci)))
                   :else (str (vec (concat oldkunci (repeat (- vjsoal cok) "-")))))
        newjenis (cond
                   (= vjsoal cok) (str oldjenis)
                   (< vjsoal cok) (str (vec (take vjsoal oldjenis)))
                   :else (str (vec (concat oldjenis (repeat (- vjsoal cok) "1")))))
        newupto (cond
                   (= vjsoal cok) (str oldupto)
                   (< vjsoal cok) (str (vec (take vjsoal oldupto)))
                   :else (str (vec (concat oldupto (repeat (- vjsoal cok) jumpil)))))
         newpretext (if oldpretext
                     (cond
                     (= vjsoal cok) (str oldpretext)
                     (< vjsoal cok) (str (vec (take vjsoal oldpretext)))
                     :else (str (vec (concat oldpretext (repeat (- vjsoal cok) "-"))))) nil)
         newsound (if oldsound
                    (cond
                     (= vjsoal cok) (str oldsound)
                     (< vjsoal cok) (str (vec (take vjsoal oldsound)))
                     :else (str (vec (concat oldsound (repeat (- vjsoal cok) "-"))))) nil)
        newpolaacak (if (= vjsoal cok)
                        (datum :polaacak)
                        (str (vec (range 1 (inc vjsoal)))))]
    (try
      (updatedata/set-soal kode gol ket vjsoal waktu jumpil newkunci
                           newjenis newupto skala nbenar nsalah newpretext
                           newsound newpolaacak)
      (layout/render "pesan.html" {:pesan (str "Berhasil update set soal!")})
         (catch Exception ex
                    (layout/render "pesan.html" {:pesan (str "Gagal update set soal! error: " ex)})))))

(defn acak-soal [kode kkode]
  (let [datum (getdata/state-acak kode)
        namakat (getdata/nama-kat kkode)]
    (layout/render "acak-soal.html" {:datum datum :namakat namakat})))

(defn update-acak-proset [kode acak polaacak]
  (if (not (or (= acak "0") (= acak "1")))
      (layout/render "pesan.html" {:pesan "Status acak harusnya 0 atau 1!"})
      (let [jsoal (:jsoal (getdata/proset-all kode))
            vpola (read-string polaacak)
            status (= (range 1 (inc jsoal)) (flatten vpola))
            ]
        (if (not status)
          (layout/render "pesan.html" {:pesan "Pola acak tidak valid!"})
          (try
             (updatedata/acak-proset kode acak polaacak)
             (layout/render "pesan.html" {:pesan "Berhasil update status acak!"})
            (catch Exception ex
              (layout/render "pesan.html" {:pesan (str "Gagal update status acak! Error:" ex)})))))))

(defn aktif-soal [kode kkode]
  (let [datum (getdata/aktif-soal kode)
        namakat (getdata/nama-kat kkode)]
    (layout/render "aktif-soal.html" {:datum datum :namakat namakat})))

(defn set-aktif-proset [kode kkode status]
  (let [datum (getdata/proset-all kode)
        oldstatus (:status datum)
        kodex (:kodex datum)]
    ;(println (str kode " " kodex " " kkode))
    (if (= status oldstatus)
        (layout/render "pesan.html" {:pesan "Status tidak berubah!"})
        (try
          (updatedata/aktif-proset kode status)
          (do
            (if (= status "0")
              (.renameTo (File. (str "resources/public/proset/" kkode "/" kode))
                         (File. (str "resources/public/proset/" kkode "/" kodex)))
              (.renameTo (File. (str "resources/public/proset/" kkode "/" kodex))
                         (File. (str "resources/public/proset/" kkode "/" kode))))
            (layout/render "pesan.html" {:pesan (str "Berhasil update keaktifan proset!")}))
          (catch Exception ex
           (layout/render "pesan.html" {:pesan (str "Gagal update keaktifan proset! error: " ex)}))))))

(defn lihat-persoal [kode kkode]
  (let [datum (getdata/proset-all kode)
        ip (:value (getdata/constant "ipnumber"))
        status (datum :status)
        kodesoal (if (= status "0") (datum :kodex) kode)
        namakat (getdata/nama-kat kkode)
        ;predir "soal/"
        predir "resources/public/proset/"
        page (cond
               (= (datum :golongan) "1") "lihat-persoal-image.html"
               (= (datum :golongan) "2") "lihat-persoal-image.html"
               :else "lihat-persoal-zenpres.html")]
    (layout/render page
                   {:datum datum
                    :kunci (read-string (datum :kunci))
                    :nsoal (vec (range 1 (inc (datum :jsoal))))
                    :npretext (read-string (datum :pretext))
                    :nsound (read-string (datum :sound))
                    :kodesoal kodesoal
                    :golongan (datum :golongan)
                    :namakat namakat
                    :path (str ip "/" predir kkode "/" kodesoal "/")
                    ;:path (str  predir kkode "/" kodesoal "/")
                    :kkode kkode
                    })))

(defn justonce [kode kkode]
  (let [datum (getdata/justonce kode)
        namakat (getdata/nama-kat kkode)]
    (layout/render "justonce.html" {:datum datum :namakat namakat})))

(defn update-justonce [kode justonce]
  (if (not (or (= justonce "0") (= justonce "1")))
      (layout/render "pesan.html" {:pesan "Status Hanya Boleh Sekali harusnya 0 atau 1!"})
          (try
             (updatedata/justonce kode justonce)
             (layout/render "pesan.html" {:pesan "Berhasil update status hanya boleh sekali!"})
            (catch Exception ex
              (layout/render "pesan.html" {:pesan (str "Gagal update status hanya boleh sekali! Error:" ex)})))))

(defn response-soal [kkode kodesoal nomer]
  (let [predir "resources/public/proset/"]
  (file-response (str predir kkode "/" kodesoal "/" nomer ".html"))))

(defn pilih-npsn-kelas [kode kkode act]
  (let [namakat (getdata/nama-kat kkode)]
    (layout/render "pilih-npsn-kelas.html" {:namakat namakat :kode kode :action act})))

(defn view-hasil-proset [kode namakat npsn kelas mode]
  (let [mdata (getdata/mdata-proset kode)
        keterangan (:keterangan mdata)
        max-row (read-string (:value (getdata/constant "max-row")))
        data (getdata/hasil-proset kode npsn kelas (inc max-row) 0)
        cdata1 (count data)
        ]
    ;(println data)
    (if (= mode 1)
        (if (< cdata1 (inc max-row))
            (layout/render "hasil-proset.html" {:data data
                                                :kode kode
                                                :namakat namakat
                                                :npsn npsn
                                                :kelas kelas
                                                :keterangan keterangan
                                                })
           (let [cdata (:total (getdata/total-hasil-proset kode npsn kelas))
                 data0 (butlast data)]
              (layout/render "total-hasil-proset.html" {:data data0
                                                        :kode kode
                                                        :namakat namakat
                                                        :npsn npsn
                                                        :kelas kelas
                                                        :keterangan keterangan
                                                        :cdata cdata
                                                        :page 0
                                                        :maxi max-row})))
      (let [datax (vec (butlast data))
          vdata (map (fn [x] [(:nis x)
                              (:name x)
                              (:npsn x)
                              (:kelas x)
                              (:nilai x)]) datax)
          header [["NIS","NAMA","NPSN" "KELAS","NILAI"]]
          dataxcel (vec (concat header vdata))
          vkls (st/replace kelas #" " "")
          filename (str "hasil-proset-npsn" npsn "-" kode "-kelas" vkls ".xlsx")
          wb (create-workbook (str "kelas" vkls) dataxcel)]
      (save-workbook! (str "dokumen/" filename) wb)
      ;(layout/render "admin/pesan.html" {:pesan "coba lihat di dokumen/coba1.xls"})
      (resp/redirect (str "/files/" filename))
      ))))

(defn view-hasil-proset-newpage [newpage cdata maxi kode npsn kelas namakat keterangan]
  (let [limit (read-string maxi)
        offset (* (read-string newpage) limit)
        data (getdata/hasil-proset kode npsn kelas limit offset)]
       (layout/render "total-hasil-proset.html" {:data data
                                                 :kode kode
                                                 :namakat namakat
                                                 :npsn npsn
                                                 :kelas kelas
                                                 :keterangan keterangan
                                                 :cdata (read-string cdata)
                                                 :page (read-string newpage)
                                                 :maxi limit})))


(defn pilih-csv []
  (layout/render "pilih-csv.html"))

(defn handle-input-siswa [file]
  (let [pw-default (:value (getdata/constant "pw-default"))
        data (slurp (:tempfile file))
        sdata (st/replace data #"\n" "")
        sdata1 (st/replace sdata #";" ",")
        vdata (map #(st/split % #",") (st/split sdata1 #"\r"))
        ]
        ;(println vdata)
        (loop [i 0 v []]
          (if (= i (count vdata))
            (if (= v [])
              (layout/render "pesan.html" {:pesan "Data siswa telah masuk semua!"})
              (let [s (apply str (interpose "," v))]
                (layout/render "pesan.html" {:pesan (str "Data yang tidak dimasukkan karena sudah ada
                                                        adalah data dengan NIS: " s)})))
            ;(do
            (let [nis_ada (getdata/nis-ada (st/trimr ((nth vdata i) 0)))]
                (if (not nis_ada)
                  (do
                    (insertdata/users
                                   (st/trim ((nth vdata i) 0)) ;nis
                                   ((nth vdata i) 1) ;nama
  ;;                                  ((nth vdata i) 2) ;email
  ;;                                  ((nth vdata i) 3) ;npsn
  ;;                                  ((nth vdata i) 4) ;kelas
                                   ((nth vdata i) 2) ;npsn
                                   ((nth vdata i) 3) ;kelas
                                   (share/create-kode 32) ;vcode
                                   (crypt/encrypt pw-default)  ;pass
                                    )
                    (recur (inc i) v))
                  (recur (inc i) (conj v (:nis nis_ada)))))
              ))))

(defn search-siswa []
  (layout/render "search-siswa.html"))

(defn search-siswa1 [act]
  (layout/render "search-siswa1.html" {:action act}))

(defn handle-list-nama [nm nis]
  (let [upnm (st/upper-case nm)
        max-row (read-string (:value (getdata/constant "max-row")))
        cdata (:jumlah (getdata/jumlah-nama upnm nis))
        data (getdata/search-nama upnm nis "name" max-row 0)]
       (if data
         (layout/render "list-siswa-nama.html"
                        {:data data
                         :cdata cdata
                         :urut "nama"
                         :vnama upnm
                         :nis nis
                         :page 0
                         :maxi max-row})
         (layout/render "pesan.html" {:pesan "Tidak ada nama atau nis tersebut!"}))
    ))

(defn list-siswa-newpage [urut newpage vnama nis cdata maxi]
  (let [vmaxi (read-string maxi)
        voffset (* (read-string newpage) vmaxi)
        data (getdata/search-nama vnama nis urut vmaxi voffset)]
    (layout/render "list-siswa-nama.html"
                   {:data data
                    :cdata (read-string cdata)
                    :urut urut
                    :vnama vnama
                    :nis nis
                    :page (read-string newpage)
                    :maxi vmaxi})))

(defn handle-do-edit-siswa [nis]
  (let [datum (getdata/users-all nis)]
    (layout/render "edit-data-siswa.html"
                 {:datum datum})))

(defn handle-update-data-siswa [nislama nisbaru nama kelas email npsn]
  (try (updatedata/users nislama nisbaru nama kelas email npsn)
               (layout/render "pesan.html" {:pesan "Berhasil mengubah data siswa!"})
               (catch Exception ex
                (layout/render "pesan.html" {:pesan "Gagal mengubah data siswa!"}))))

(defn handle-delete-data-siswa [nis]
  (try (deletedata/users nis)
       (layout/render "pesan.html"
                      {:pesan (str "Berhasil menghapus data siswa dengan nis = " nis)})
    (catch Exception ex
      (layout/render "pesan.html" {:pesan (str "Gagal menghapus data siswa! error: " ex)}))))

(defn input-admin []
  (layout/render "input-admin.html"))

(defn do-input-admin [id status pass]
  (if (or (= id "") (= status "") (= pass ""))
      (layout/render "pesan.html" {:pesan "Gagal menambah admin/operator!"})
      (try
        (insertdata/admin id status pass)
        (layout/render "pesan.html" {:pesan "Berhasil menambah admin/operator!"})
        (catch Exception ex
          (layout/render "pesan.html"
                         {:pesan (str "Gagal menambah admin/operator! error: " ex)})))))

(defn tambah-paket-sbmptn []
  (layout/render "tambah-paket-sbmptn.html"))

(defn handle-tambah-paket-sbmptn [ket tkpa jurusan kelompok]
  (try
    (insertdata/simsbmptn ket tkpa jurusan kelompok)
    (layout/render "pesan.html" {:pesan "Berhasil menambah paket SBMPTN!"})
          (catch Exception ex
             (layout/render "pesan.html" {:pesan (str "Gagal menambah paket SBMPTN! error" ex)}))))

(defn list-paket-sbmptn [act]
  (let [data (getdata/simsbmptn-all)]
       (layout/render "list-paket-sbmptn.html" {:data data :action act})))

(defn edit-paket-sbmptn [kode]
  (let [datum (getdata/simsbmptn kode)]
    (layout/render "edit-paket-sbmptn.html" {:datum datum})))

(defn update-paket-sbmptn [kode ket tkpa ipaorips kelompok]
  (try (updatedata/simsbmptn kode ket tkpa ipaorips kelompok)
               (layout/render "pesan.html" {:pesan "Berhasil mengubah paket SBMPTN!"})
               (catch Exception ex
                (layout/render "pesan.html" {:pesan (str "Gagal mengubah paket SBMPTN! error:" ex)}))))

(defn delete-paket-sbmptn [kode]
  (try (deletedata/simsbmptn kode)
       (layout/render "pesan.html"
                      {:pesan (str "Berhasil menghapus paket SBMPTN!")})
    (catch Exception ex
      (layout/render "pesan.html" {:pesan (str "Gagal menghapus paket SBMPTN! error: " ex)}))))

(defn input-sekolah []
  (layout/render "input-sekolah.html"))

(defn tambah-sekolah [npsn nama alamat telpon email]
  (if (or (= npsn "") (= nama ""))
      (layout/render "input-sekolah.html" {:error "NPSN atau Nama Sekolah tidak diisi!"
                                           :npsn npsn
                                           :nama nama
                                           :alamat alamat
                                           :telpon telpon
                                           :email email})
      (try
         (insertdata/sekolah npsn nama alamat telpon email)
         (layout/render "pesan.html" {:pesan (str "Berhasil menambah sekolah dengan nama " nama)})
         (catch Exception ex
         (layout/render "pesan.html" {:pesan "Gagal menambah Sekolah!"})))))


(defn search-sekolah []
  (layout/render "search-sekolah.html"))

(defn view-sekolah [npsn nama]
  (let [max-row (:value (getdata/constant "max-row"))
        data (getdata/sekolah npsn (st/upper-case nama) max-row)]
    (if data
      (layout/render "view-sekolah-edit.html" {:data data})
      (layout/render "pesan.html" {:pesan "Tidak ditemukan data sekolah tersebut!"}))))

(defn edit-sekolah [npsn]
  (let [datum (getdata/sekolah-npsn npsn)]
    (layout/render "edit-sekolah.html" {:datum datum})))

(defn update-sekolah [npsn nama alamat telpon email nomer]
  (try
    (updatedata/sekolah npsn nama alamat telpon email nomer)
    (layout/render "pesan.html" {:pesan "Berhasil Update Sekolah!"})
    (catch Exception ex
     (layout/render "pesan.html" {:pesan (str "Gagal Update Sekolah! error: " ex)}))))

(defn input-paket-ppdb []
  (layout/render "tambah-paket-ppdb.html"))

(defn tambah-paket-ppdb [ket mat ipa ind ing]
  (try
    (insertdata/simppdb ket mat ipa ind ing)
    (layout/render "pesan.html" {:pesan "Berhasil menambah paket ppdb!"})
          (catch Exception ex
             (layout/render "pesan.html" {:pesan (str "Gagal menambah paket ppdb! error" ex)}))))

(defn list-paket-ppdb [act]
  (let [data (getdata/simppdb-all)]
       (layout/render "list-paket-ppdb.html" {:data data :action act})))

(defn edit-paket-ppdb [kode]
  (let [datum (getdata/simppdb-kode kode)]
    (layout/render "edit-paket-ppdb.html" {:datum datum})))

(defn update-paket-ppdb [kode ket mat ipa ind ing]
  (try (updatedata/simppdb kode ket mat ipa ind ing)
               (layout/render "pesan.html" {:pesan "Berhasil mengubah paket PPDB!"})
               (catch Exception ex
                (layout/render "pesan.html" {:pesan (str "Gagal mengubah paket PPDB! error:" ex)}))))

(defn delete-paket-ppdb [kode]
  (try (deletedata/simppdb kode)
       (layout/render "pesan.html"
                      {:pesan (str "Berhasil menghapus paket PPDB!")})
    (catch Exception ex
      (layout/render "pesan.html" {:pesan (str "Gagal menghapus paket PPDB! error: " ex)}))))

(defn tambah-sma-ppdb []
  (let [daerah (getdata/daerah)]
       (layout/render "tambah-sma-ppdb.html" {:daerah daerah})))

(defn tambah-sma-ppdb1 [kode sek nm]
  (try
    (insertdata/pgsma kode sek nm)
    (layout/render "pesan.html" {:pesan "Berhasil menambah data SMA!"})
          (catch Exception ex
             (layout/render "pesan.html" {:pesan (str "Gagal menambah data SMA! error" ex)}))))

(defn search-sma-ppdb [act]
  (layout/render "search-sma-ppdb.html" {:action act}))

(defn list-sma-ppdb [nama]
  (let [data (getdata/pgsma-nama (st/upper-case nama))]
       (layout/render "list-sma-ppdb.html" {:data data})))

(defn edit-sma-ppdb [nomer]
  (let [daerah (getdata/daerah)
        datasek (getdata/sekolah-pgsma nomer)]
    (layout/render "edit-sma-ppdb.html" {:daerah daerah :datasek datasek})))

(defn update-sma-ppdb [nomer sekolah kode nm]
  (try (updatedata/pgsma nomer sekolah kode nm)
               (layout/render "pesan.html" {:pesan "Berhasil mengubah data SMA!"})
               (catch Exception ex
                (layout/render "pesan.html" {:pesan (str "Gagal mengubah data SMA! error:" ex)}))))

(defn delete-sma-ppdb [nomer]
  (try (deletedata/pgsma nomer)
       (layout/render "pesan.html"
                      {:pesan (str "Berhasil menghapus data SMA!")})
    (catch Exception ex
      (layout/render "pesan.html" {:pesan (str "Gagal menghapus data SMA! error: " ex)}))))

(defn input-daerah []
  (layout/render "tambah-daerah.html"))

(defn tambah-daerah [daerah]
  (try
    (insertdata/kodedaerah daerah)
    (layout/render "pesan.html" {:pesan "Berhasil menambah data daerah!"})
          (catch Exception ex
             (layout/render "pesan.html" {:pesan (str "Gagal menambah data daerah! error" ex)}))))

(defn search-daerah [act]
  (layout/render "search-daerah-ppdb.html" {:action act}))

(defn list-daerah-ppdb [nama]
  (let [data (getdata/daerah-nama (st/upper-case nama))]
    (layout/render "list-daerah.html" {:data data})))

(defn edit-daerah-ppdb [kode]
  (let [datum (getdata/kodedaerah kode)]
    (layout/render "edit-daerah-ppdb.html" {:datum datum})))

(defn update-daerah-ppdb [kode daerah]
  (try (updatedata/kodedaerah kode daerah)
               (layout/render "pesan.html" {:pesan "Berhasil mengubah nama daerah!"})
               (catch Exception ex
                (layout/render "pesan.html" {:pesan (str "Gagal mengubah nama daerah! error:" ex)}))))

;;; Hasil PPDB
(defn save-to-dokumen [filename]
  (file-response (str "dokumen/" filename)))

(defn hasil-pilih-npsn-kelas [kode act]
  (let [namakat (:keterangan (getdata/simppdb-kode kode))]
    (layout/render "pilih-npsn-kelas.html" {:kode kode :namakat namakat :action act})))

(defn num-to-str [number dk]
  (-> (format (str "%." dk "f") (* number 1.0))
      (clojure.string/replace #"\." ",")))

(defn jbenar [sa sb]
    (reduce + (map #(if (= %1 %2) 1 0) (read-string sa) (read-string sb))))

(defn jkosong [sa]
  (count (filter #(= "-" %) (read-string sa))))

(defn jsalah [sa sb]
  (reduce + (map #(if (and (not= "-" %1) (not= %1 %2)) 1 0) (read-string sa) (read-string sb))))

(defn proses-laporan-ppdb [kodepaket keterangan npsn kelas mode]
  (let [
        ;sekolah (:nasek (db/get-data (str "select nasek from sekolah where kode='" kosek "'") 1))
        ;ckosek (count kosek)
        max-row (read-string (:value (getdata/constant "max-row")))
        paket keterangan
        vkodepaket (getdata/simppdb-kodepaket kodepaket)
        kodemat (:kodemat vkodepaket)
        kodeipa (:kodeipa vkodepaket)
        kodeind (:kodeind vkodepaket)
        kodeing (:kodeing vkodepaket)
        kuncimat (:kunci (getdata/kunci kodemat))
        kunciipa (:kunci (getdata/kunci kodeipa))
        kunciind (:kunci (getdata/kunci kodeind))
        kunciing (:kunci (getdata/kunci kodeing))
        vsiswa (getdata/simppdb-peserta kodemat kodeipa kodeind kodeing npsn kelas)
        csis (count vsiswa)
        vnmat (getdata/simppdb-nilai kodemat npsn kelas)
        vnipa (getdata/simppdb-nilai kodeipa npsn kelas)
        vnind (getdata/simppdb-nilai kodeind npsn kelas)
        vning (getdata/simppdb-nilai kodeing npsn kelas)
        vnmat1 (map (fn [x] {:nis (:nis x)
                             :nilai (:nilai x)
                             :B (jbenar (:jawaban x) kuncimat)
                             :S (jsalah (:jawaban x) kuncimat)
                             :K (jkosong (:jawaban x))}) vnmat)
        vnipa1 (map (fn [x] {:nis (:nis x)
                             :nilai (:nilai x)
                             :B (jbenar (:jawaban x) kunciipa)
                             :S (jsalah (:jawaban x) kunciipa)
                             :K (jkosong (:jawaban x))}) vnipa)
        vnind1 (map (fn [x] {:nis (:nis x)
                             :nilai (:nilai x)
                             :B (jbenar (:jawaban x) kunciind)
                             :S (jsalah (:jawaban x) kunciind)
                             :K (jkosong (:jawaban x))}) vnind)
        vning1 (map (fn [x] {:nis (:nis x)
                             :nilai (:nilai x)
                             :B (jbenar (:jawaban x) kunciing)
                             :S (jsalah (:jawaban x) kunciing)
                             :K (jkosong (:jawaban x))}) vning)

        daftar (loop [a [] i 0]
                 (if (= i csis)
                   a
                   (let [m-sis (nth vsiswa i)
                         m-mat (first (filter #(= (:nis m-sis) (:nis %)) vnmat1))
                         m-ipa (first (filter #(= (:nis m-sis) (:nis %)) vnipa1))
                         m-ind (first (filter #(= (:nis m-sis) (:nis %)) vnind1))
                         m-ing (first (filter #(= (:nis m-sis) (:nis %)) vning1))
                         ntot (+ (if m-mat (:nilai m-mat) 0)
                                 (if m-ipa (:nilai m-ipa) 0)
                                 (if m-ind (:nilai m-ind) 0)
                                 (if m-ing (:nilai m-ing) 0))
                         nis (:nis m-sis)
                         nama (:name m-sis)
                         npsn (:npsn m-sis)
                         kelas (:kelas m-sis)
                         nmat (if m-mat (:nilai m-mat) " ")
                         Bmat (if m-mat (:B m-mat) " ")
                         Smat (if m-mat (:S m-mat) " ")
                         Kmat (if m-mat (:K m-mat) " ")
                         nipa (if m-ipa (:nilai m-ipa) " ")
                         Bipa (if m-ipa (:B m-ipa) " ")
                         Sipa (if m-ipa (:S m-ipa) " ")
                         Kipa (if m-ipa (:K m-ipa) " ")
                         nind (if m-ind (:nilai m-ind) " ")
                         Bind (if m-ind (:B m-ind) " ")
                         Sind (if m-ind (:S m-ind) " ")
                         Kind (if m-ind (:K m-ind) " ")
                         ning (if m-ing (:nilai m-ing) " ")
                         Bing (if m-ing (:B m-ing) " ")
                         Sing (if m-ing (:S m-ing) " ")
                         King (if m-ing (:K m-ing) " ")
                         ]
                     (recur (conj a {:nis nis :npsn npsn
                                     :nama nama :ntot ntot :kelas kelas
                                     :nmat (if (= nmat " ") " " (num-to-str nmat 2)) :Bmat Bmat :Smat Smat :Kmat Kmat
                                     :nipa (if (= nipa " ") " " (num-to-str nipa 2)) :Bipa Bipa :Sipa Sipa :Kipa Kipa
                                     :nind (if (= nind " ") " " (num-to-str nind 2)) :Bind Bind :Sind Sind :Kind Kind
                                     :ning (if (= ning " ") " " (num-to-str ning 2)) :Bing Bing :Sing Sing :King King})
                            (inc i)))))
        daftar1a (reverse (sort-by :ntot daftar))
        daftar1 (if (<= max-row (count daftar1a)) daftar1a (take max-row daftar1a))
        daftar2 (map #(update-in %1 [:ntot] num-to-str 2) daftar1)
        ]
    (if (= mode 1)
    (layout/render "hasil-test-ppdb.html" {:data (vec daftar2)
                                                 :paket paket
                                                 :npsn npsn})
    (let [datax (vec daftar2)
          vdata (map (fn [x] [(:nis x)
                              (:nama x)
                              (:npsn x)
                              (:kelas x)
                              (:Bmat x) (:Smat x) (:Kmat x) (:nmat x)
                              (:Bipa x) (:Sipa x) (:Kipa x) (:nipa x)
                              (:Bind x) (:Sind x) (:Kind x) (:nind x)
                              (:Bing x) (:Sing x) (:King x) (:ning x)
                              (:ntot x)]) datax)
          header [["NIS","NAMA","NPSN" "KELAS","MATEMATIKA","","","","SAINS","","","","INDONESIA","","","","INGGRIS","","","","TOTAL"]
                  ["" "" "" "" "B" "S" "K" "NIL" "B" "S" "K" "NIL" "B" "S" "K" "NIL" "B" "S" "K" "NIL" ""]
                   ]
          dataxcel (vec (concat header vdata))
          vkls (st/replace kelas #" " "")
          filename (str "ppdb-npsn" npsn "-" kodepaket "-kelas" vkls ".xlsx")
          wb (create-workbook (str "kelas" vkls) dataxcel)]
      (save-workbook! (str "dokumen/" filename) wb)
      ;(layout/render "admin/pesan.html" {:pesan "coba lihat di dokumen/coba1.xls"})
      (resp/redirect (str "/files/" filename))
      )
    )))

(defn sbmptn-pilih-npsn-kelas [kode act]
  (let [namakat (:keterangan (getdata/simsbmptn kode))]
    (layout/render "pilih-npsn-kelas.html" {:kode kode :namakat namakat :action act})))

(defn proses-laporan-sbmptn [kodepaket keterangan npsn kelas mode]
  (let [
        ;sekolah (:nasek (db/get-data (str "select nasek from sekolah where kode='" kosek "'") 1))
        ;ckosek (count kosek)
        max-row (read-string (:value (getdata/constant "max-row")))
        paket keterangan
        vkodepaket (getdata/simsbmptn kodepaket)
        kodetkpa (:kodetkpa vkodepaket)
        kodeipaorips (:kodeipaorips vkodepaket)
        kelompok (:kelompok vkodepaket)
        skel (if (= "1" kelompok) "SAINS" "SOSHUM")
        kuncitkpa (:kunci (getdata/kunci kodetkpa))
        kunciipaorips (:kunci (getdata/kunci kodeipaorips))


        vsiswa (getdata/sbmptn-peserta kodetkpa kodeipaorips npsn kelas)
        csis (count vsiswa)
        vntkpa (getdata/simppdb-nilai kodetkpa npsn kelas)
        vnipaorips (getdata/simppdb-nilai kodeipaorips npsn kelas)

        vntkpa1 (map (fn [x] {:nis (:nis x)
                             :nilai (:nilai x)
                             :B (jbenar (:jawaban x) kuncitkpa)
                             :S (jsalah (:jawaban x) kuncitkpa)
                             :K (jkosong (:jawaban x))}) vntkpa)
        vnipaorips1 (map (fn [x] {:nis (:nis x)
                             :nilai (:nilai x)
                             :B (jbenar (:jawaban x) kunciipaorips)
                             :S (jsalah (:jawaban x) kunciipaorips)
                             :K (jkosong (:jawaban x))}) vnipaorips)

        daftar (loop [a [] i 0]
                 (if (= i csis)
                   a
                   (let [m-sis (nth vsiswa i)
                         m-tkpa (first (filter #(= (:nis m-sis) (:nis %)) vntkpa1))
                         m-ipaorips  (first (filter #(= (:nis m-sis) (:nis %)) vnipaorips1))

                         ntot (/ (+ (if m-tkpa (:nilai m-tkpa) 0)
                                 (if m-ipaorips (:nilai m-ipaorips) 0)) 2)
                         nis (:nis m-sis)
                         nama (:name m-sis)
                         npsn (:npsn m-sis)
                         kelas (:kelas m-sis)
                         ntkpa (if m-tkpa (:nilai m-tkpa) " ")
                         Btkpa (if m-tkpa (:B m-tkpa) " ")
                         Stkpa (if m-tkpa (:S m-tkpa) " ")
                         Ktkpa (if m-tkpa (:K m-tkpa) " ")
                         nipaorips (if m-ipaorips (:nilai m-ipaorips) " ")
                         Bipaorips (if m-ipaorips (:B m-ipaorips) " ")
                         Sipaorips (if m-ipaorips (:S m-ipaorips) " ")
                         Kipaorips (if m-ipaorips (:K m-ipaorips) " ")
                         ]
                     (recur (conj a {:nis nis :npsn npsn
                                     :nama nama :ntot ntot :kelas kelas
                                     :ntkpa (if (= ntkpa " ") " " (num-to-str ntkpa 2))
                                     :Btkpa Btkpa :Stkpa Stkpa :Ktkpa Ktkpa
                                     :nipaorips (if (= nipaorips " ") " " (num-to-str nipaorips 2))
                                     :Bipaorips Bipaorips :Sipaorips Sipaorips :Kipaorips Kipaorips
                                     })
                            (inc i)))))
        daftar1a (reverse (sort-by :ntot daftar))
        daftar1 (if (<= max-row (count daftar1a)) daftar1a (take max-row daftar1a))
        daftar2 (map #(update-in %1 [:ntot] num-to-str 2) daftar1)
        ]
    (if (= mode 1)
    (layout/render "hasil-test-sbmptn.html" {:data (vec daftar2)
                                             :paket paket
                                             :npsn npsn
                                             :skel skel})
    (let [datax (vec daftar2)
          vdata (map (fn [x] [(:nis x)
                              (:nama x)
                              (:npsn x)
                              (:kelas x)
                              (:Btkpa x) (:Stkpa x) (:Ktkpa x) (:ntkpa x)
                              (:Bipaorips x) (:Sipaorips x) (:Kipaorips x) (:nipaorips x)
                              (:ntot x)]) datax)
          header [["NIS","NAMA","NPSN" "KELAS","TKPA","","","", skel ,"","","","TOTAL"]
                  ["" "" "" "" "B" "S" "K" "NIL" "B" "S" "K" "NIL" ""]
                   ]
          dataxcel (vec (concat header vdata))
          vkls (st/replace kelas #" " "")
          filename (str "sbmptn-npsn" npsn "-" kodepaket "-kelas" vkls ".xlsx")
          wb (create-workbook (str "kelas" vkls) dataxcel)]
      (save-workbook! (str "dokumen/" filename) wb)
      ;(layout/render "admin/pesan.html" {:pesan "coba lihat di dokumen/coba1.xls"})
      (resp/redirect (str "/files/" filename))
      )
    )))

(defn hitung-nilai [jawaban kunci nbenar nsalah skala]
  (let [jsoal (count kunci)
        jbenar (loop [jb 0, i 0]
                          (if (= i jsoal)
                              jb
                              (recur (if (= (jawaban i) (kunci i)) (inc jb) jb) (inc i))))
        jkosong (count (filter #(= % "-") jawaban))
        jsalah (- jsoal (+ jbenar jkosong))
        nilai (/ (Math/round (* (/ (+ (* jbenar nbenar) (* jsalah nsalah)) (* jsoal nbenar)) skala 100.0)) 100.0)]
    nilai))

(defn hitung-ulang [kode]
  (let [dproset (getdata/proset-all kode)
        dtest (getdata/datato-nis-jawaban kode)
        kunci (read-string (:kunci dproset))
        skala (:skala dproset)
        nbenar (:nbenar dproset)
        nsalah (:nsalah dproset)]
        ;(println (nth dtest 0))
        ;(do
        (doseq [dsiswa dtest]
              (let [vjawab (read-string (dsiswa :jawaban))
                    vnis (dsiswa :nis)
                    nilai (hitung-nilai vjawab kunci nbenar nsalah skala)]
                    (updatedata/nilai kode vnis nilai)))
          ;(layout/render "admin/pesan.html" {:pesan "Hitung ulang sudah selesai!"})
    ))

(defn input-kode []
  (layout/render "input-kode.html"))

(defn hitung-ulang-hasil [kode]
  (try
    (hitung-ulang kode)
    (layout/render "pesan.html" {:pesan "Selesai hitung ulang hasil test!"})
    (catch Exception ex
          (layout/render "pesan.html" {:pesan (str "Gagal hitung ulang hasil! error:" ex)}))))

(defn hitung-bsk [no kun dt]
  (loop [[b s k] [0 0 0], j 0]
       (if (= j (count dt))
           [k b s]
           (recur
             (cond
                (= ((read-string (:jawaban (nth dt j))) no) "-") [b s (inc k)]
                (= ((read-string (:jawaban (nth dt j))) no) kun) [(inc b) s k]
                :else [b (inc s) k])
             (inc j))
         )))

(defn abs-bsk [kode kkode mode]
  (let [proset (getdata/proset-all kode)
        data (getdata/datato-jawaban kode)
        kunci (read-string (:kunci proset))
        jsoal (:jsoal proset)
        namakat (getdata/nama-kat kkode)
        keterangan (:keterangan proset)
        vhasil (loop [hsl [], i 0]
                     (if (= i jsoal)
                         hsl
                         (let [v (hitung-bsk i (kunci i) data)] (recur (conj hsl v) (inc i)))))
        ]
        (if (= mode 1)
          (layout/render "abs-bsk.html"
                                {:namakat namakat
                                 :keterangan keterangan
                                 :kode kode
                                 :peserta (count data)
                                 :hasil vhasil})
          (let [vdata (vec (map #(vec (cons %1 %2)) (range 1 (inc jsoal)) vhasil))
                header [["DISTRIBUSI BENAR SALAH KOSONG"]
                        [(str "TO " namakat ", kode:" kode "-" keterangan)]
                        ["NO","BENAR","SALAH" "KOSONG"]]
          dataxcel (vec (concat header vdata))
          filename (str "abs-bsk-" kkode "-" kode ".xlsx")
          wb (create-workbook "abs-bsk" dataxcel)]
      (save-workbook! (str "dokumen/" filename) wb)
      ;(layout/render "admin/pesan.html" {:pesan "coba lihat di dokumen/coba1.xls"})
      (resp/redirect (str "/files/" filename))
      ))))

(defn abs-tk [kode kkode mode]
  (let [proset (getdata/proset-all kode)
        data (getdata/datato-jawaban kode)
        kunci (read-string (:kunci proset))
        jsoal (:jsoal proset)
        namakat (getdata/nama-kat kkode)
        keterangan (:keterangan proset)
        ;;;Analisis Tingkat Kesulitan
        datatk (getdata/datato-jawaban-tk kode)
        jdatatk (count datatk)
        jU (Math/round (* jdatatk 0.25))
        datatk1 (map #(% :jawaban) (concat (take jU datatk) (drop (- jdatatk jU) datatk)))
        cdatatk1 (count datatk1)
        vtk (loop [vt [] i 0]
              (if (= i (count kunci))
                  vt
                  (let [kun (kunci i)
                        jbi (count
                              (filter (fn [x] (= x true))
                                (map #(= kun ((read-string %) i)) datatk1)))
                        tk (/ (Math/round (/ jbi cdatatk1 0.01)) 100.0)]
                        (recur (conj vt tk) (inc i)))))
        ]
        (if (= mode 1)
          (layout/render "abs-tk.html"
                                {:namakat namakat
                                 :keterangan keterangan
                                 :kode kode
                                 :peserta (count data)
                                 :hasil vtk})
          (let [vdata0 (partition 2 (interleave (range 1 (inc jsoal)) vtk))
                vdata (vec (map #(vec %) vdata0))
                header [["ANALISIS TINGKAT KESULITAN"]
                        [(str "TO " namakat ", kode:" kode "-" keterangan)]
                        ["NO","TINGKAT KESULITAN"]]
          dataxcel (vec (concat header vdata))
          filename (str "abs-tk-" kkode "-" kode ".xlsx")
          wb (create-workbook "abs-tk" dataxcel)]
      (save-workbook! (str "dokumen/" filename) wb)
      ;(layout/render "admin/pesan.html" {:pesan "coba lihat di dokumen/coba1.xls"})
      (resp/redirect (str "/files/" filename))
      ))))

(defn abs-dp [kode kkode mode]
  (let [proset (getdata/proset-all kode)
        data (getdata/datato-jawaban kode)
        kunci (read-string (:kunci proset))
        jsoal (:jsoal proset)
        namakat (getdata/nama-kat kkode)
        keterangan (:keterangan proset)
        ;;;Analisis Daya Pembeda
        datatk (getdata/datato-jawaban-tk kode)
        jdatatk (count datatk)
        jU (Math/round (* jdatatk 0.25))
        datatkU (map #(% :jawaban) (take jU datatk))
        datatkL (map #(% :jawaban) (drop (- jdatatk jU) datatk))

        vdp (loop [dp [] i 0]
              (if (= i (count kunci))
                  dp
                  (let [kun (kunci i)
                        jbiU (count
                               (filter (fn [x] (= x true))
                                 (map #(= kun ((read-string %) i)) datatkU)))
                        jbiL (count
                               (filter (fn [x] (= x true))
                                 (map #(= kun ((read-string %) i)) datatkL)))
                        tdp (/ (Math/round (/ (- jbiU jbiL) jU 0.01)) 100.0)] (recur (conj dp tdp) (inc i)))))
        ]
        (if (= mode 1)
          (layout/render "abs-dp.html"
                                {:namakat namakat
                                 :keterangan keterangan
                                 :kode kode
                                 :peserta (count data)
                                 :hasil vdp})
          (let [vdata0 (partition 2 (interleave (range 1 (inc jsoal)) vdp))
                vdata (vec (map #(vec %) vdata0))
                header [["ANALISIS DAYA PEMBEDA"]
                        [(str "TO " namakat ", kode:" kode "-" keterangan)]
                        ["NO","DAYA PEMBEDA"]]
          dataxcel (vec (concat header vdata))
          filename (str "abs-dp-" kkode "-" kode ".xlsx")
          wb (create-workbook "abs-dp" dataxcel)]
      (save-workbook! (str "dokumen/" filename) wb)
      ;(layout/render "admin/pesan.html" {:pesan "coba lihat di dokumen/coba1.xls"})
      (resp/redirect (str "/files/" filename))
      ))))

(defn hitung-abc [no kun dt jp]
  (if (= jp 4)
    (loop [[a b c d k] [0 0 0 0 0], j 0]
         (if (= j (count dt))
             [kun a b c d k]
             (recur
               (cond
                  (= ((read-string (:jawaban (nth dt j))) no) "-") [a b c d (inc k)]
                  (= ((read-string (:jawaban (nth dt j))) no) "A") [(inc a) b c d k]
                  (= ((read-string (:jawaban (nth dt j))) no) "B") [a (inc b) c d k]
                  (= ((read-string (:jawaban (nth dt j))) no) "C") [a b (inc c) d k]
                  (= ((read-string (:jawaban (nth dt j))) no) "D") [a b c (inc d) k]
                 :else [a b c d (inc k)]
                )
               (inc j))))

    (loop [[a b c d e k] [0 0 0 0 0 0], j 0]
         (if (= j (count dt))
             [kun a b c d e k]
             (recur
               (cond
                  (= ((read-string (:jawaban (nth dt j))) no) "-") [a b c d e (inc k)]
                  (= ((read-string (:jawaban (nth dt j))) no) "A") [(inc a) b c d e k]
                  (= ((read-string (:jawaban (nth dt j))) no) "B") [a (inc b) c d e k]
                  (= ((read-string (:jawaban (nth dt j))) no) "C") [a b (inc c) d e k]
                  (= ((read-string (:jawaban (nth dt j))) no) "D") [a b c (inc d) e k]
                  (= ((read-string (:jawaban (nth dt j))) no) "E") [a b c d (inc e) k]
                 :else [a b c d e (inc k)]
                )
               (inc j))))))

(defn abs-dk [kode kkode mode]
  (let [proset (getdata/proset-all kode)
        upto (read-string (:upto proset))
        dupto (distinct upto)
        jumpil (read-string (first dupto))]
        (if (and (= (count dupto) 1) (>= jumpil 4))
            (let [data (getdata/datato-jawaban kode)
                  kunci (read-string (:kunci proset))
                  jsoal (:jsoal proset)
                  namakat (getdata/nama-kat kkode)
                  keterangan (:keterangan proset)
                  vhasil (loop [hsl [], i 0]
                         (if (= i jsoal)
                             hsl
                             (let [v (hitung-abc i (kunci i) data jumpil)]
                                  (recur (conj hsl v) (inc i)))))]
                  (if (= mode 1)
                    (layout/render "abs-dk.html"
                                          {:namakat namakat
                                           :keterangan keterangan
                                           :kode kode
                                           :peserta (count data)
                                           :jumpil jumpil
                                           :hasil vhasil})
                    (let [vdata (vec (map #(vec (cons %1 %2)) (range 1 (inc jsoal)) vhasil))
                          urow (if (= jumpil 4)
                                   ["NO" "KUNCI" "PIL A" "PIL B" "PIL C" "PIL D" "KOSONG"]
                                   ["NO" "KUNCI" "PIL A" "PIL B" "PIL C" "PIL D" "PIL E" "KOSONG"]
                                 )
                          header [["ANALISIS DAYA KECOH"]
                                  [(str "TO " namakat ", kode:" kode "-" keterangan)]
                                  urow]
                    dataxcel (vec (concat header vdata))
                    filename (str "abs-dk-" kkode "-" kode ".xlsx")
                    wb (create-workbook "abs-dk" dataxcel)]
                    (save-workbook! (str "dokumen/" filename) wb)
                    ;(layout/render "admin/pesan.html" {:pesan "coba lihat di dokumen/coba1.xls"})
                    (resp/redirect (str "/files/" filename))
                    )))
               (layout/render "pesan.html" {:pesan "Tidak dapat dilakukan Analisis
                                            Daya Kecoh untuk proset ini!"})
          )))
