(ns tonline.routes.home
  (:require [compojure.core :refer :all]
            [tonline.views.layout :as layout]
            ;[noir.validation :as vali]
            ;[noir.util.crypt :as crypt]
            [noir.response :as resp]
            [tonline.controllers.chome :as chome]
            ;[noir.session :as session]
            ;[tonline.models.share :as share]
            ))

(defroutes home-routes

  (POST "/login" [id pass]
       (chome/login id pass))

  (GET "/logout" []
       (chome/logout))

  (GET "/ganti-password" []
       (chome/ganti-pw))
  (POST "/ganti-pw1" [pwlama pwbaru1 pwbaru2]
        (chome/ganti-pw-user pwlama pwbaru1 pwbaru2))

  (GET "/" []
       (chome/home))

  (GET "/set-dimg" []
       (chome/set-constant "ipnumber" "Domain Image" "Domain Image"))

  (GET "/set-dapp" []
       (chome/set-constant "dserver-app" "Domain Aplikasi" "Domain Aplikasi"))

  (GET "/set-max-row" []
       (chome/set-constant "max-row" "Baris Maksimum Data" "Baris Maksimum"))

  (GET "/set-pw-default" []
       (chome/set-constant "pw-default" "Password Default Siswa" "Password Default"))

  (POST "/update-constant" [nama value judul]
        (chome/update-constant nama value judul))

  ;;; Proset management
  (GET "/daftarkan-proset" []
       (chome/daftarkan-proset))
  (POST "/daftarkan-proset" [gol kodekategori ket jsoal waktu jumpil]
        (chome/simpan-proset gol kodekategori ket jsoal waktu jumpil))

  (GET "/edit-kunci" []
       (chome/search-proset "/edit-search-proset"))
  (POST "/edit-search-proset" [kode ket]
        (chome/handle-search-proset kode ket "/edit-kunci"))
  (POST "/edit-kunci" [kode kkode]
        (chome/edit-kunci kode kkode))
  (POST "/simpan-kunci" [kode jenis upto kunci]
        (chome/save-kunci kode jenis upto kunci))
  (POST "/simpan-kunci-image" [kunci jenis upto pretext sound kode]
        (chome/save-kunci-image kunci jenis upto pretext sound kode))

   (GET "/upload-file" []
        (chome/search-proset "/upload-list-proset"))
   (POST "/upload-list-proset" [kode ket]
         (chome/handle-search-proset kode ket "/create-directory"))
   (POST "/create-directory" [kode kkode]
         (chome/create-directory kode kkode "/upload-file"))
   (POST "/upload-file" [file kode kkode]
         (chome/upload-file file kode kkode))

  (GET "/import-file-zp" []
       (chome/search-proset "/import-search-proset"))
  (POST "/import-search-proset" [kode ket]
        (chome/handle-search-proset kode ket "/import-create-directory"))
  (POST "/import-create-directory" [kode kkode]
        (chome/import-create-directory kode kkode "/import-soal-zp"))
  (POST "/import-soal-zp" [kode kodex kkode namakat ket jsoal no soal]
        (chome/handle-import-soal-zp kode kodex kkode namakat ket jsoal no soal))

   (GET "/upload-gambar" []
        (chome/take-gambar))
   (POST "/upload-gambar" [file]
         (chome/upload-gambar file))

   (GET "/edit-set-soal" []
        (chome/search-proset "/edit-set-list-soal"))
   (POST "/edit-set-list-soal" [kode ket]
         (chome/handle-search-proset kode ket "/edit-set-soal"))
   (POST "/edit-set-soal" [kode kkode]
         (chome/edit-set-soal kode kkode))
   (POST "/update-set-soal" [kode gol ket jsoal waktu jumpil skala nbenar nsalah]
         (chome/update-set-soal kode gol ket jsoal waktu jumpil skala nbenar nsalah))

   (GET "/acak-soal" []
        (chome/search-proset "/acak-search-proset"))
   (POST "/acak-search-proset" [kode ket]
         (chome/handle-search-proset kode ket "/acak-soal"))
   (POST "/acak-soal" [kode kkode]
         (chome/acak-soal kode kkode))
   (POST "/update-acak-proset" [kode acak polaacak]
         (chome/update-acak-proset kode acak polaacak))

   (GET "/aktif-soal" []
        (chome/search-proset "/aktif-search-proset"))
   (POST "/aktif-search-proset" [kode ket]
         (chome/handle-search-proset kode ket "/aktif-soal"))
   (POST "/aktif-soal" [kode kkode]
         (chome/aktif-soal kode kkode))
   (POST "/update-aktif-proset" [kode kkode status]
         (chome/set-aktif-proset kode kkode status))

   (GET "/lihat-persoal" []
        (chome/search-proset "/persoal-search-proset"))
   (POST "/persoal-search-proset" [kode ket]
         (chome/handle-search-proset kode ket "/lihat-persoal"))
   (POST "/lihat-persoal" [kode kkode]
         (chome/lihat-persoal kode kkode))

  (POST "/get-soal" [nset folder nomer]
       (chome/proset nset folder nomer))

  (GET "/response-soal/:kkode/:kodesoal/:nomer" [kkode kodesoal nomer]
       (chome/response-soal kkode kodesoal nomer))

  (GET "/justonce" []
       (chome/search-proset "/justonce-search-proset"))
  (POST "/justonce-search-proset" [kode ket]
         (chome/handle-search-proset kode ket "/justonce"))
  (POST "/justonce" [kode kkode]
         (chome/justonce kode kkode))
  (POST "/update-justonce" [kode justonce]
         (chome/update-justonce kode justonce))

  ;;; Hasil Test
  (GET "/hasil-proset" []
       (chome/search-proset "/hasil-search-proset"))
  (POST "/hasil-search-proset" [kode ket]
         (chome/handle-search-proset kode ket "/pilih-npsn-kelas"))
  (POST "/pilih-npsn-kelas" [kode kkode]
        (chome/pilih-npsn-kelas kode kkode "/view-hasil-proset"))
  (POST "/view-hasil-proset" [kode namakat npsn kelas]
        (chome/view-hasil-proset kode namakat npsn kelas 1))

  (POST "/list-hasil-newpage" [newpage cdata maxi kode npsn kelas namakat keterangan]
        (chome/view-hasil-proset-newpage newpage cdata maxi kode npsn kelas namakat keterangan))

  ;;; Siswa
  (GET "/input-siswa" []
       (chome/pilih-csv))
  (POST "/input-siswa" [file]
        (chome/handle-input-siswa file))

  (GET "/edit-siswa" []
       (chome/search-siswa1 "/edit-siswa"))
  (POST "/edit-siswa" [nama nis]
        (chome/handle-list-nama nama nis))
  (POST "/do-edit-siswa" [nis]
        (chome/handle-do-edit-siswa nis))
  (POST "/update-data-siswa" [nislama nisbaru nama kelas email npsn]
        (chome/handle-update-data-siswa nislama nisbaru nama kelas email npsn))
  (POST "/delete-data-siswa" [nislama]
        (chome/handle-delete-data-siswa nislama))
  (POST "/list-siswa-newpage" [urut newpage vnama nis cdata maxi]
        (chome/list-siswa-newpage urut newpage vnama nis cdata maxi))

  ;;; Admin
  (GET "/input-admin" []
       (chome/input-admin))
  (POST "/input-admin" [id status pass]
        (chome/do-input-admin id status pass))

  ;;; Paket SBMPTN
  (GET "/tambah-paket-sbmptn" []
       (chome/tambah-paket-sbmptn))
  (POST "/tambah-paket-sbmptn" [ket tkpa jurusan kelompok]
        (chome/handle-tambah-paket-sbmptn ket tkpa jurusan kelompok))

  (GET "/edit-paket-sbmptn" []
       (chome/list-paket-sbmptn "/edit-paket-sbmptn"))
  (POST "/edit-paket-sbmptn" [kode]
        (chome/edit-paket-sbmptn kode))
  (POST "/update-paket-sbmptn" [kode ket tkpa ipaorips kelompok]
        (chome/update-paket-sbmptn kode ket tkpa ipaorips kelompok))
  (POST "/delete-paket-sbmptn" [kode]
        (chome/delete-paket-sbmptn kode))

  ;;; Data Sekolah
  (GET "/tambah-sekolah" []
       (chome/input-sekolah))
  (POST "/tambah-sekolah" [npsn nama alamat telpon email]
       (chome/tambah-sekolah npsn nama alamat telpon email))

  (GET "/edit-sekolah" []
       (chome/search-sekolah))
  (POST "/list-sekolah" [npsn nama]
        (chome/view-sekolah npsn nama))
  (POST "/edit-sekolah" [npsn]
        (chome/edit-sekolah npsn))
  (POST "/update-sekolah" [npsn nama alamat telpon email nomer]
        (chome/update-sekolah npsn nama alamat telpon email nomer))

  ;;; Data PPDB SMP to SMA
  (GET "/tambah-paket-ppdb" []
       (chome/input-paket-ppdb))
  (POST "/tambah-paket-ppdb" [ket mat ipa ind ing]
        (chome/tambah-paket-ppdb ket mat ipa ind ing))

  (GET "/edit-paket-ppdb" []
       (chome/list-paket-ppdb "/edit-paket-ppdb"))
  (POST "/edit-paket-ppdb" [kode]
        (chome/edit-paket-ppdb kode))
  (POST "/update-paket-ppdb" [kode ket mat ipa ind ing]
        (chome/update-paket-ppdb kode ket mat ipa ind ing))
  (POST "/delete-paket-ppdb" [kode]
        (chome/delete-paket-ppdb kode))

   (GET "/tambah-sma-ppdb" []
       (chome/tambah-sma-ppdb))
  (POST "/tambah-sma-ppdb" [kodedaerah sekolah nm]
        (chome/tambah-sma-ppdb1 kodedaerah sekolah nm))

  (GET "/edit-sma-ppdb" []
       (chome/search-sma-ppdb "/list-sma-ppdb"))
  (POST "/list-sma-ppdb" [nama]
        (chome/list-sma-ppdb nama))
  (POST "/edit-sma-ppdb" [nomer]
        (chome/edit-sma-ppdb nomer))

  (POST "/update-sma-ppdb" [nomer sekolah kode nm]
        (chome/update-sma-ppdb nomer sekolah kode nm))
  (POST "/delete-sma-ppdb" [nomer]
        (chome/delete-sma-ppdb nomer))

  (GET "/tambah-daerah-ppdb" []
       (chome/input-daerah))
  (POST "/tambah-daerah" [daerah]
        (chome/tambah-daerah daerah))

  (GET "/edit-daerah-ppdb" []
       (chome/search-daerah "/edit-daerah-ppdb"))
  (POST "/edit-daerah-ppdb" [nama]
       (chome/list-daerah-ppdb nama))
  (POST "/do-edit-daerah-ppdb" [kode]
        (chome/edit-daerah-ppdb kode))
  (POST "/update-daerah-ppdb" [kode daerah]
        (chome/update-daerah-ppdb kode daerah))

  ;;; Hasil PPDB
  (GET "/pilih-paket-ppdb" []
       (chome/list-paket-ppdb "/hasil-pilih-npsn-kelas"))
  (POST "/hasil-pilih-npsn-kelas" [kode]
        (chome/hasil-pilih-npsn-kelas kode "/hasil-test-ppdb"))
  (POST "/hasil-test-ppdb" [kode namakat npsn kelas]
        (chome/proses-laporan-ppdb kode namakat npsn kelas 1))

  ;;; HASIL SBMPTN
  (GET "/pilih-paket-sbmptn" []
       (chome/list-paket-sbmptn "/sbmptn-pilih-npsn-kelas"))
  (POST "/sbmptn-pilih-npsn-kelas" [kode]
        (chome/sbmptn-pilih-npsn-kelas kode "/hasil-test-sbmptn"))
  (POST "/hasil-test-sbmptn" [kode namakat npsn kelas]
        (chome/proses-laporan-sbmptn kode namakat npsn kelas 1))

  ;;; Simpan ke Excell
  (GET "/hasil-test-excel" []
       (chome/search-proset "/hasil-search-proset-excel"))
  (POST "/hasil-search-proset-excel" [kode ket]
         (chome/handle-search-proset kode ket "/pilih-npsn-kelas-excel"))
  (POST "/pilih-npsn-kelas-excel" [kode kkode]
        (chome/pilih-npsn-kelas kode kkode "/view-hasil-proset-excel"))
  (POST "/view-hasil-proset-excel" [kode namakat npsn kelas]
        (chome/view-hasil-proset kode namakat npsn kelas 0))

  (GET "/paket-ppdb-excell" []
       (chome/list-paket-ppdb "/excell-pilih-npsn-kelas"))
  (POST "/excell-pilih-npsn-kelas" [kode]
        (chome/hasil-pilih-npsn-kelas kode "/excell-test-ppdb"))
  (POST "/excell-test-ppdb" [kode namakat npsn kelas]
        (chome/proses-laporan-ppdb kode namakat npsn kelas 0))

  (GET "/paket-sbmptn-excell" []
       (chome/list-paket-sbmptn "/sbmptn-pilih-npsn-kelas-excell"))
  (POST "/sbmptn-pilih-npsn-kelas-excell" [kode]
        (chome/sbmptn-pilih-npsn-kelas kode "/hasil-test-sbmptn-excell"))
  (POST "/hasil-test-sbmptn-excell" [kode namakat npsn kelas]
        (chome/proses-laporan-sbmptn kode namakat npsn kelas 0))

  (GET "/files/:filename" [filename]
       (chome/save-to-dokumen filename))

  ;;;Hitung Ulang Hasil
  (GET "/hitung-ulang-hasil" []
       (chome/input-kode))
  (POST "/hitung-ulang-hasil" [kode]
        (chome/hitung-ulang-hasil kode))

  ;;;Analisis Butir Soal
  (GET "/abs-bsk" []
       (chome/search-proset "/bsk-search-proset"))
  (POST "/bsk-search-proset" [kode ket]
         (chome/handle-search-proset kode ket "/abs-bsk"))
  (POST "/abs-bsk" [kode kkode]
         (chome/abs-bsk kode kkode 1))

  (GET "/abs-bsk-excel" []
       (chome/search-proset "/bsk-search-proset-excel"))
  (POST "/bsk-search-proset-excel" [kode ket]
         (chome/handle-search-proset kode ket "/abs-bsk-excel"))
  (POST "/abs-bsk-excel" [kode kkode]
         (chome/abs-bsk kode kkode 0))

  (GET "/abs-tk" []
       (chome/search-proset "/tk-search-proset"))
  (POST "/tk-search-proset" [kode ket]
         (chome/handle-search-proset kode ket "/abs-tk"))
  (POST "/abs-tk" [kode kkode]
         (chome/abs-tk kode kkode 1))

  (GET "/abs-tk-excel" []
       (chome/search-proset "/tk-search-proset-excel"))
  (POST "/tk-search-proset-excel" [kode ket]
         (chome/handle-search-proset kode ket "/abs-tk-excel"))
  (POST "/abs-tk-excel" [kode kkode]
         (chome/abs-tk kode kkode 0))

  (GET "/abs-dp" []
       (chome/search-proset "/dp-search-proset"))
  (POST "/dp-search-proset" [kode ket]
         (chome/handle-search-proset kode ket "/abs-dp"))
  (POST "/abs-dp" [kode kkode]
         (chome/abs-dp kode kkode 1))

  (GET "/abs-dp-excel" []
       (chome/search-proset "/dp-search-proset-excel"))
  (POST "/dp-search-proset-excel" [kode ket]
         (chome/handle-search-proset kode ket "/abs-dp-excel"))
  (POST "/abs-dp-excel" [kode kkode]
         (chome/abs-dp kode kkode 0))

  (GET "/abs-dk" []
       (chome/search-proset "/dk-search-proset"))
  (POST "/dk-search-proset" [kode ket]
         (chome/handle-search-proset kode ket "/abs-dk"))
  (POST "/abs-dk" [kode kkode]
         (chome/abs-dk kode kkode 1))

  (GET "/abs-dk-excel" []
       (chome/search-proset "/dk-search-proset-excel"))
  (POST "/dk-search-proset-excel" [kode ket]
         (chome/handle-search-proset kode ket "/abs-dk-excel"))
  (POST "/abs-dk-excel" [kode kkode]
         (chome/abs-dk kode kkode 0))

  (GET "/data/:soal" [soal]
       (resp/json {:soal (slurp (str "./html/" soal ".html") ) :jwb "A"}))

)
