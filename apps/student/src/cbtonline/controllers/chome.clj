(ns cbtonline.controllers.chome
  (:require [compojure.core :refer :all]
            [cbtonline.views.layout :as layout]
            [cbtonline.models.getdata :as getdata]
            [cbtonline.models.insertdata :as insertdata]
            [cbtonline.models.updatedata :as updatedata]
            [cbtonline.shares.share :as share]
            [noir.validation :as vali]
            [noir.util.crypt :as crypt]
            [noir.response :as resp]
            [noir.session :as session]
            [postal.core :as postal]
            [clojure.string :as st]
            [ring.util.response :refer [file-response]]
            ;[noir.io :as io]
            )
  ;(:import (java.io File))
  )

(defn num-to-str [number dk]
  (-> (format (str "%." dk "f") (* number 1.0))
      (st/replace #"\." ",")))

(defn send-email [nis pass vcode email]
    (let [dserver-app (:value (getdata/dserver-app))
          route_email (str dserver-app "/activation/")]
     (postal/send-message
                           {:host "smtp.webfaction.com"
                            :user "zeniuscbt"
                            :ssl true
                            :pass "zeniuscbt2000"}
                               {:from "noreply@zeniuscbt.com"
                                :to email
                                :subject "Aktivasi Akun Zenius CBT"
                                :body [:alternative
                                       {:type "text/plain"
                                        :content "Terimakasih telah mendaftar di zeniuscbt.com."}
                                       {:type "text/html"
                                        :content (str "<html><head> </head>
                                        <body><p>Selamat, anda telah terdaftar di zeniuscbt.com. Data anda:</p>
                                              <p>NIS :" nis "</p>
                                              <p>Password :" pass "</p>
                                              <br>
                                              <p>Agar akun anda dapat diaktifkan sehingga dapat login ke zeniuscbt.com, klik
                                                 link di bawah ini!</p>
                                              <p><a href=" route_email nis "/" vcode ">" route_email nis "/" vcode "</a></p>
                                        </body></html>")}
                                      ]})
        (layout/render "pesan.html" {:pesan "Anda telah berhasil mendaftar di zeniuscbt.com. Silahkan buka
                                     email anda untuk mengaktivasi akun anda !"})))

(defn send-email-nis [nis email]
  (postal/send-message  {:host "smtp.webfaction.com"
                         :user "zeniuscbt"
                         :ssl true
                         :pass "zeniuscbt2000"}
                               {:from "noreply@zeniuscbt.com"
                                :to email
                                :subject "Lupa NIS akun zeniuscbt.com"
                                :body [:alternative
                                       {:type "text/plain"
                                        :content "Lupa NIS"}
                                       {:type "text/html"
                                        :content (str "<html><head> </head>
                                        <body><p>Email ini diterima karena Anda lupa NIS akun zeniuscbt anda</p>
                                              <p>Nis anda adalah " nis "</p>
                                        </body></html>")}
                                      ]}))

(defn send-email-pw [vcode email]
  (let [dserver-app (:value (getdata/dserver-app))
        route_email (str dserver-app "/gantipwlupa/")]
  (postal/send-message  {:host "smtp.webfaction.com"
                         :user "zeniuscbt"
                         :ssl true
                         :pass "zeniuscbt2000"}
                               {:from "noreply@zeniuscbt.com"
                                :to email
                                :subject "Lupa password akun zeniuscbt.com"
                                :body [:alternative
                                       {:type "text/plain"
                                        :content "Lupa password"}
                                       {:type "text/html"
                                        :content (str "<html><head> </head>
                                        <body><p>Email ini diterima karena Anda telah meminta password baru untuk akun zeniuscbt.com</p>
                                              <p>Jika tidak merasa meminta password baru karena lupa, maka abaikan email ini</p>
                                              <p>Klik link di bawah jika anda mau meneruskan untuk mengganti password akun
                                                      zeniuscbt</p>
                                              <br>
                                              <p><a href= " route_email email "/" vcode ">" route_email email "/" vcode "</a></p>
                                        </body></html>")}
                                      ]})))

(defn aktivasi-lagi [nis]
    (let [dserver-app (:value (getdata/dserver-app))
          route_email (str dserver-app "/activation/")
          user (getdata/user-login nis)
          email (:email user)
          vcode (:vcode user)]
     (postal/send-message
                           {:host "smtp.webfaction.com"
                            :user "zeniuscbt"
                            :ssl true
                            :pass "zeniuscbt2000"}
                               {:from "noreply@zeniuscbt.com"
                                :to email
                                :subject "Aktivasi Akun Zenius CBT"
                                :body [:alternative
                                       {:type "text/plain"
                                        :content "Link Aktivasi account Zenius CBT"}
                                       {:type "text/html"
                                        :content (str "<html><head> </head>
                                        <body><p>Agar akun anda dapat diaktifkan sehingga dapat login ke zeniuscbt.com, klik
                                                 link di bawah ini!</p>
                                              <p><a href=" route_email nis "/" vcode ">" route_email nis "/" vcode "</a></p>
                                        </body></html>")}
                                      ]})
        (layout/render "pesan.html" {:pesan "Silahkan buka email anda untuk mengaktivasi akun anda !"})))

(defn home []
  (layout/render "login.html" {:action "/home-login"}))

(defn home-login []
  (layout/render "home.html"))

(defn registrasi-siswa
    [& [npsn nis nama email db-error]]
    (layout/render "registrasi-siswa.html"
        { :npsn npsn
          :nis nis
          :nama nama
          :email email
          :db-error db-error}))

(defn handle-sign-up-student
  [npsn nis nama email pass pass1]
  (let [npsn-error (cond
                     (= npsn "") "NPSN harus diisi!"
                     (not= (count npsn) (count (re-seq #"[0-9]" npsn))) "NPSN harus berupa angka!"
                     :else "")
        nis-error (cond
                    (= nis "") "NIS harus diisi!"
                    (not= (count nis) (count (re-seq #"[0-9]" nis))) "NIS harus berupa angka!"
                     :else "")
        nama-error (if (= nama "") "Nama harus diisi!" "")
        email-error (if (= email "") "Email harus diisi!" "")
        pass-error (if (< (count pass) 5) "Password harus lebih dari 5 huruf!" "")
        pass1-error (if (not= pass pass1) "Password tidak bersesuaian!" "")
        datum (getdata/nis-email nis email)
        ne-error (if datum "NIS atau Email sudah terdaftar!" "")
        verrors (filter #(not= "" %)
                  [npsn-error nis-error nama-error email-error pass-error pass1-error ne-error])
        errors (cond
                 (empty? verrors) ""
                 (= 1 (count verrors)) (apply str verrors)
                 :else (apply str (interpose ", " verrors)))
        vpass (crypt/encrypt pass)
        vcode (share/create-kode 32)]
    (if (= "" errors)
      (try
        (insertdata/user npsn nis nama email vpass vcode)
        ;(layout/render "pesan.html" {:pesan "Registrasi Sukses!"})
        (send-email nis pass vcode email)
        (catch Exception ex
          (registrasi-siswa npsn nis nama email (str "Registrasi gagal, ulangi sesaat lagi!" ex))
          ;(registrasi-siswa)
          ))
      (registrasi-siswa npsn nis nama email errors))))

(defn login-activation [nis]
   (let [user (getdata/user-login nis)]
      (do
         (session/put! :user nis)
         (session/put! :nama (:name user))
         (resp/redirect "/home"))))

(defn handle-activation [nis vcode]
  (let [data (getdata/nis-vcode nis vcode)]
    (if (= (data :vcode) vcode)
        (try (updatedata/user-activation nis)
       ;(login-activation nis)
       (resp/redirect "/")
       (catch Exception ex
        (layout/render "pesan.html"
          {:pesan (str "Gagal Aktivasi Akun error: " ex)})))
       (layout/render "pesan.html" {:pesan "Kode tidak cocok!"})
      )))

(defn handle-login [nis pass]
  (let [user (getdata/user-login nis)
        email (:email user)
        aktif (:activation user)]
      (if user
        (if (= aktif "1")
         (if (crypt/compare pass (:pass user))
           (if (not email)
           (layout/render "input-email.html" {:nis (user :nis)})
           (do
             (session/put! :user nis)
             (session/put! :nama (user :name))
             ;(session/put! :ip ip)
             ;(session/put! :status 4)
             (layout/render "home.html")))
           (layout/render "login.html"
                          {:error "Password Salah!" :nis nis :action "/home-login"}))
          (layout/render "aktivasi-lagi.html" {:nis nis}))
         (layout/render "login.html"
                          {:error "Tidak ada user dengan NIS tersebut!"
                           :nis nis :action "/home-login"}))))

(defn ganti-pw []
  (let [user (session/get :user)]
    (if user
      (layout/render "ganti-pw.html")
      (resp/redirect "/"))))

(defn ganti-pw-siswa [pwlama pwbaru1 pwbaru2]
  (let [pwnow (:pass (getdata/get-password (session/get :user)))]
    (if (or (not (crypt/compare  pwlama pwnow)) (< (count pwbaru1) 5))
        (layout/render "ganti-pw.html" {:error "Password Lama tidak benar atau password baru kurang dari lima huruf!"})
        (if (= pwbaru1 pwbaru2)
          (try (updatedata/password (crypt/encrypt pwbaru1) (session/get :user))
                 (do
                    (session/clear!)
                    (resp/redirect "/"))
               (catch Exception ex
                  (layout/render "ganti-pw.html" {:error "Gagal ganti password!"})))
          (layout/render "ganti-pw.html" {:error "Password tidak sesuai!"})))))

(defn lupa-pass-nis []
  (layout/render "lupa-pw-nis.html"))

(defn find-pw-nis [kode email]
  (let [value (getdata/nis-pw email)]
    (if value
      (if (= kode "2")
        (do
          (send-email-nis (:nis value) email)
          (layout/render "pesan.html" {:pesan "Silahkan buka email untuk melihat NIS anda"}))
        (do
          (send-email-pw (:vcode value) email)
          (layout/render "pesan.html" {:pesan "Anda telah meminta password baru karena lupa password. Buka email anda untuk langkah
                                              lebih lanjut!"})))

      (layout/render "pesan.html" {:pesan "Tidak ada email tersebut!"}))))

(defn ganti-pw-lupa [email vcode]
  (layout/render "transit.html" {:email email :vcode vcode}))
(defn ganti-pw-lupa0 [email vcode]
  (layout/render "ganti-pw-lupa.html" {:email email :vcode vcode}))

(defn ganti-pw-lupa1 [email vcode pass1 pass2]
  (if (< (count pass1) 5)
    (layout/render "ganti-pw-lupa.html"
                   {:email email :vcode vcode :error "Password harus lebih dari 5 huruf!"})
    (if (not= pass1 pass2)
      (layout/render "ganti-pw-lupa.html"
                     {:email email :vcode vcode :error "Password tidak bersesuaian!"})
      (try
        (updatedata/pwlupa email vcode (crypt/encrypt pass1))
        (resp/redirect "/")
        (catch Exception ex
                  (layout/render "ganti-pw-lupa.html"
                                 {:email email :vcode vcode :error (str "Gagal ganti password! " ex)}))))))

(defn nolstore []
  (let [user (session/get :user)]
    (if user
        (layout/render "kode1.html")
        (resp/redirect "/"))))

(defn lstore []
  (layout/render "kode2.html"))

(defn logout [page]
  (do
   (session/clear!)
   (resp/redirect page)))

(defn acak [pola]
  (let [p1 (if (not-any? integer? pola) pola (shuffle pola))
        p2 (map #(if (vector? %)
                     (shuffle %)
                    %) p1)]
    (flatten p2)))

(defn grab-paket
  "Get Problem Set Package"
  [kodeto]
  (let [fkodeto (filter #(and (< (int %) 58) (> (int %) 47)) kodeto)
        f-kd (not= (count kodeto) (count fkodeto))
        f-nol-d (= \0 (first fkodeto))
        f-kosong (= "" kodeto)]
     (if (or f-kd f-nol-d f-kosong)
       (layout/render "kode1.html" {:error "Paket Soal dengan kode tersebut tidak ada!" :kodeto kodeto})
       (let [data (getdata/proset-all kodeto)]
          (if (not data)
              (layout/render "kode1.html" {:error "Paket Soal dengan kode tersebut tidak ada!" :kodeto kodeto})
              (let [sudah (getdata/datato (session/get :user) kodeto)
                    justonce (data :justonce)
                    bolehto (or (not sudah) (= justonce "0"))
                    status (data :status)]
                   (if (= status "1")
                     (if bolehto
                       (let [ip (:value (getdata/ip))
                             kkode (data :kodekategori)
                             path (str ip "/resources/public/proset/" kkode "/" kodeto "/")
                             jsoal (data :jsoal)
                             acak? (= "1" (data :acak))
                             vupto (read-string (data :upto))
                             vjenis (read-string (data :jenis))
                             vpretext (if (data :pretext) (read-string (data :pretext)) (repeat jsoal "-"))
                             vsound (if (data :sound) (read-string (data :sound)) (repeat jsoal "-"))
                             nsoal (vec (if acak?
                                            (acak (read-string (data :polaacak)))
                                            (range 1 (inc jsoal))))
                             vsemua (if acak?
                                       (loop [v [] i 0]
                                         (if (= i jsoal)
                                             v
                                             (recur
                                                 (conj v [(nth vupto (dec (nsoal i)))
                                                          (nth vjenis (dec (nsoal i)))
                                                          (nth vpretext (dec (nsoal i)))
                                                          (nth vsound (dec (nsoal i)))])
                                                 (inc i))))
                                       "dummy")
                             nupto (if acak? (vec (map #(first %) vsemua)) vupto)
                             njenis (if acak? (vec (map #(second %) vsemua)) vjenis)
                             npretext (if acak? (vec (map #(nth % 2) vsemua)) vpretext)
                             nsound (if acak? (vec (map #(last %) vsemua)) vsound)
                             page (cond
                                    (= (data :golongan) "1") "tryout1.html"
                                    (= (data :golongan) "2") "tryout1.html"
                                    :else "tryout3.html")
                             user (session/get :user)
                             ]
                            ;(println nsoal)
                            (layout/render page {:data data
                                                 :nsoal nsoal
                                                 :njenis njenis
                                                 :nupto nupto
                                                 :npretext npretext
                                                 :nsound nsound
                                                 :kodeto kodeto
                                                 :kkode kkode
                                                 :path path}
                                                 :user user
                                                 ))
                       (layout/render "pesan.html" {:pesan (str "Kamu sudah pernah mengerjakan TO dengan kode " kodeto " ini!")}))
                     (layout/render "kode1.html" {:error "Paket Soal dengan kode tersebut tidak ada!" :kodeto kodeto}))
              ))))))

(defn response-soal [kkode kodeto nomer]
  (let [predir "resources/public/proset/"]
  (file-response (str predir kkode "/" kodeto "/" nomer ".html"))))

(defn tm-simpan-jawaban [kode jawab ni]
  (let [nis ni
        jawaban (st/split jawab #":")
        dproset (getdata/proset-all kode)
        ada (getdata/datato nis kode)

        jsoal (count jawaban)
        kunci (read-string (:kunci dproset))

        jbenar (loop [jb 0, i 0]
                          (if (= i jsoal)
                              jb
                              (recur (if (= (jawaban i) (kunci i)) (inc jb) jb) (inc i))))
        jkosong (count (filter #(= % "-") jawaban))
        jsalah (- jsoal (+ jbenar jkosong))
        skala (:skala dproset)
        nbenar (:nbenar dproset)
        nsalah (:nsalah dproset)
        nilai (/ (Math/round (* (/ (+ (* jbenar nbenar) (* jsalah nsalah)) (* jsoal nbenar)) skala 100.0)) 100.0)
        ]
         (if (not ada)
             (try (insertdata/datato nis
                                     (Integer/parseInt kode)
                                     (str jawaban)
                                     nilai
                                     (java.sql.Timestamp. (.getTime (java.util.Date.))))
              {:nilai nilai :skala skala}
               ;{:nilai nil}
              (catch Exception ex
                {:nilai nil :skala skala}))
             (try (updatedata/datato nis
                                     (Integer/parseInt kode)
                                     (str jawaban)
                                     nilai
                                     (java.sql.Timestamp. (.getTime (java.util.Date.))))
               {:nilai nilai :skala skala}
               (catch Exception ex
                {:nilai nil :skala skala}))
           )))

(defn to-lanjutan [kode]
  (let [data (getdata/kategori-keterangan kode)
        ip (:value (getdata/ip))
        kkode (data :kodekategori)
        path (str ip "/resources/public/proset/" kkode "/" kode "/")
        page (cond
                (= (data :golongan) "1") "tryout-lanjutan1.html"
                (= (data :golongan) "2") "tryout-lanjutan1.html"
                :else "tryout-lanjutan3.html")]
  ;(println data)
  (layout/render page {:data data
                       :kodeto kode
                       :path path})))

(defn lihat-hasil [nis]
  (if nis
      (let [data (getdata/hasil-to-individu nis)
            data1 (map #(update-in %1 [:nilai] num-to-str 2) data)]
        (layout/render "list-nilai.html" {:data data1}))
    (resp/redirect "/")))

(defn pilih-paket []
  (let [user (session/get :user)]
     (if user
      (let [data (getdata/paket-simsbmptn)]
        (layout/render "pilih-paket.html" {:data data}))
       (resp/redirect "/"))))

(defn proses-paket [kode]
  (let [paket (getdata/simsbmptn-all kode)
        nis (session/get :user)
        kelompok (:kelompok paket)
        kodetkpa (:kodetkpa paket)
        kodeipaorips (:kodeipaorips paket)
        nilaitkpa (getdata/nilai-tkpa nis kodetkpa)
        nilaiipaorips (getdata/nilai-ipaorips nis kodeipaorips)]
    (if (and nilaitkpa nilaiipaorips)
        (let [ntot (/ (+ (:nilai nilaitkpa) (:nilai nilaiipaorips)) 2.0)
              univ (getdata/filter-univ ntot kelompok)
              funiv (if univ (:ptn (first univ)) "VOID")
              jurusan (if univ
                          (getdata/filter-jurusan funiv ntot kelompok)
                          "VOID")]
          (if univ
            (layout/render "list-jurusan.html"
                           {:ntot (format (str "%." 2 "f") (* ntot 1.0))
                            :jurusan jurusan
                            :univ univ
                            :kelompok kelompok
                            :sainsoshum (if (= kelompok "1") "SAINTEK" "SOSHUM")
                            :ntkpa (format (str "%." 2 "f") (* (:nilai nilaitkpa) 1.0))
                            :nipaorips (format (str "%." 2 "f") (* (:nilai nilaiipaorips) 1.0))
                            :ptn funiv})
            (layout/render "pesan.html" {:pesan "Nilai tidak memenuhi syarat di semua universitas!"})))
         (layout/render "pesan.html" {:pesan "Nilai ujian tidak lengkap!"}))))

(defn get-jurusan [ptn kelompok ntot]
  (getdata/get-jurusan ptn kelompok ntot))

(defn pilih-paket-ppdb []
  (let [data (getdata/pilih-paket-ppdb)]
    (layout/render "pilih-paket-ppdb.html" {:data data})))

(defn proses-paket-ppdb [kode]
  (let [paket (getdata/paket-ppdb-all kode)
        kodemat (:kodemat paket)
        kodeipa (:kodeipa paket)
        kodeind (:kodeind paket)
        kodeing (:kodeing paket)
        nis (session/get :user)
        nilaimat (:nilai (getdata/nilaimat-smp nis kodemat))
        nilaiipa (:nilai (getdata/nilaiipa-smp nis kodeipa))
        nilaiind (:nilai (getdata/nilaiind-smp nis kodeind))
        nilaiing (:nilai (getdata/nilaiing-smp nis kodeing))]
    (if (and nilaimat nilaiipa nilaiind nilaiing)
        (let [ntot (+ nilaimat nilaiipa nilaiind nilaiing)
              daerah (getdata/filter-daerah ntot)
              fkode (if daerah (:kode (first daerah)) "VOID")
              fdaerah (if daerah (:daerah (first daerah)) "VOID")
              sekolah (if daerah
                          (getdata/filter-sekolah fkode ntot)
                          "VOID")]
          (if daerah
            (layout/render "list-sma.html" {:ntot (format (str "%." 2 "f") (* ntot 1.0))
                                            :daerah daerah
                                            :sekolah sekolah
                                            :nmat (format (str "%." 2 "f") (* nilaimat 1.0))
                                            :nipa (format (str "%." 2 "f") (* nilaiipa 1.0))
                                            :nind (format (str "%." 2 "f") (* nilaiind 1.0))
                                            :ning (format (str "%." 2 "f") (* nilaiing 1.0))
                                            :wilayah fdaerah
                                            :kodedaerah fkode})
            (layout/render "pesan.html" {:pesan "Nilai tidak memenuhi syarat di semua Daerah!"})))
         (layout/render "pesan.html" {:pesan "Nilai ujian tidak lengkap!"}))))

;; (defn home-next-daerah [kode ntot nmat nipa nind ning]
;;   (let [daerah (db/get-data (str "select distinct pgsma.kodedaerah as kode, daerah from pgsma
;;                                      inner join kodedaerah on pgsma.kodedaerah=kodedaerah.kode
;;                                      where nm <='" ntot "' order by daerah") 2)
;;         sekolah (db/get-data (str "select sekolah,nm from pgsma where kodedaerah='" kode "' and
;;                                         nm<='" (read-string ntot) "' order by nm desc") 2)
;;         wilayah (db/get-data (str "select daerah from kodedaerah where kode='" kode "'") 1)]
;;     (layout/render "home/list-sma.html" {:ntot ntot
;;                                           :daerah daerah
;;                                              :sekolah sekolah
;;                                              :nmat nmat
;;                                              :nipa nipa
;;                                              :nind nind
;;                                              :ning ning
;;                                              :wilayah (:daerah wilayah)
;;                                              :kodedaerah kode})))

(defn get-sekolah [kode ntot]
  (getdata/get-sekolah kode ntot))

(defn update-email [nis email]
  (try
    (updatedata/email nis email)
    (resp/redirect "/")
    (catch Exception ex
        (layout/render "pesan.html" {:pesan "Gagal memasukkan email!"}))))
