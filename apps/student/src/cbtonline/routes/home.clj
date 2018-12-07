(ns cbtonline.routes.home
  (:require [compojure.core :refer :all]
            [cbtonline.views.layout :as layout]
            [noir.validation :as vali]
            [noir.util.crypt :as crypt]
            [noir.response :as resp]
            [cbtonline.controllers.chome :as chome]
            [noir.session :as session]
            ;[cbtonline.models.share :as share]
            ))

(defroutes home-routes
  (GET "/" []
       (chome/home))
  (GET "/home" []
       (chome/home-login))
  (GET "/registrasi-siswa" []
       (chome/registrasi-siswa))
  (POST "/registrasi-siswa" [npsn nis nama email pass pass1]
        (chome/handle-sign-up-student npsn nis nama email pass pass1))
  (GET "/activation/:nis/:vcode" [nis vcode]
       (chome/handle-activation nis vcode))

  (GET "/home-test" []
       (chome/nolstore))
  (GET "/gantipwlupa/:email/:vcode" [email vcode]
       (chome/ganti-pw-lupa email vcode))
  (POST "/ganti-pw-lupa0" [email vcode]
        (chome/ganti-pw-lupa0 email vcode))
  (POST "/ganti-pw-lupa1" [email vcode pass1 pass2]
        (chome/ganti-pw-lupa1 email vcode pass1 pass2))

  (POST "/home-login" [nis pass]
       (chome/handle-login nis pass))

  (POST "/home-no-lstore" []
        (chome/nolstore))

  (POST "/home-lstore" []
        (chome/lstore))

  (POST "/tryout-lanjutan" [kode]
        (chome/to-lanjutan kode))

  (POST "/tryout-baru" [kode]
        (chome/nolstore))

  (POST "/kodeto" [kodeto]
        (chome/grab-paket kodeto))

  (GET "/home-logout" []
       (chome/logout "/"))

  (GET "/ganti-pw" []
       (chome/ganti-pw))
  (POST "/ganti-pw1" [pwlama pwbaru1 pwbaru2]
        (chome/ganti-pw-siswa pwlama pwbaru1 pwbaru2))

  (GET "/lupa-password-nis" []
       (chome/lupa-pass-nis))
  (POST "/lupa-pw-nis" [kode email]
        (chome/find-pw-nis kode email))

  (POST "/tm-simpan" [kode jawaban nis]
        (resp/json (chome/tm-simpan-jawaban kode jawaban nis)))

  (GET "/lihat-hasil" []
       (chome/lihat-hasil (session/get :user)))

  (GET "/sim-sbmptn" []
       (chome/pilih-paket))
  (POST "/proses-paket" [kode]
        (chome/proses-paket kode))
  (GET "/get-jurusan/:ptn/:kelompok/:ntot" [ptn kelompok ntot]
       (resp/json (chome/get-jurusan ptn kelompok ntot)))

  (GET "/sim-ppdb" []
        (chome/pilih-paket-ppdb))
  (POST "/proses-paket-ppdb" [kode]
        (chome/proses-paket-ppdb kode))
;;   (POST "/home-next-daerah" [kode ntot nmat nipa nind ning]
;;         (home-next-daerah kode ntot nmat nipa nind ning))

  (GET "/get-sekolah/:kode/:ntot" [kode ntot]
       (resp/json (chome/get-sekolah kode ntot)))

  (GET "/response-soal/:kkode/:kode/:nomer" [kkode kode nomer]
       (chome/response-soal kkode kode nomer))

  (GET "/aktivasi-lagi/:nis" [nis]
       (chome/aktivasi-lagi nis))

  (POST "/input-email" [nis email]
        (chome/update-email nis email))
)
