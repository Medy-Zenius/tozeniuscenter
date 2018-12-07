(ns cbtonline.models.getdata
  (:require [cbtonline.models.db :as db]))

(defn nis-email [nis email]
  (db/get-data (str "select nis,email from users where nis='" nis "' or email='" email "'") 2))

(defn nis-vcode [nis vcode]
  (db/get-data (str "select nis,vcode from users where nis='" nis "'") 1))

(defn user-login [nis]
  (db/get-data (str "select nis,name,pass,activation,vcode,email from users where nis='" nis "'") 1))

(defn get-password [user]
  (db/get-data (str "select pass from users where nis='" user "'") 1))

(defn nis-pw [email]
  (db/get-data (str "select nis,vcode from users where email='" email "'") 1))

(defn proset-all [kode]
  (db/get-data (str "select * from proset where kode='" kode "'") 1))

(defn ip []
  (db/get-data "select value from constants where nama='ipnumber'" 1))

(defn dserver-app []
  (db/get-data "select value from constants where nama='dserver-app'" 1))

(defn datato [nis kode]
  (db/get-data (str "select nis from datato where nis='" nis "' and kode='" kode "'") 1))

(defn kategori-keterangan [kode]
  (db/get-data (str "select nama,kodekategori,golongan,keterangan from proset inner join kategori
                    on kategori.kode=proset.kodekategori where proset.kode='" kode "'") 1))

(defn hasil-to-individu [nis]
  (db/get-data (str
                 "select datato.kode,nilai,to_char(tanggal,'DD-MM-YYYY') as stanggal,keterangan
                 from datato inner join proset on datato.kode=proset.kode
                  where nis='" nis "' order by keterangan,tanggal desc") 2))

(defn paket-simsbmptn []
  (db/get-data "select kode,kodetkpa,kodeipaorips,keterangan from
               simsbmptn order by keterangan" 2))

(defn simsbmptn-all [kode]
  (db/get-data (str "select * from simsbmptn where kode='" kode "'") 1))

(defn nilai-tkpa [nis kodetkpa]
  (db/get-data (str "select nilai from datato where nis='" nis "'
                                    and kode='" kodetkpa "'") 1))

(defn nilai-ipaorips [nis kodeipaorips]
  (db/get-data (str "select nilai from datato where nis='" nis "'
                                       and kode='" kodeipaorips "'") 1))

(defn filter-univ [ntot kelompok]
  (db/get-data (str "select distinct ptn from pg1 where nm <='" ntot "' and
                                     kelompok='" kelompok "' order by ptn") 2))

(defn filter-jurusan [funiv ntot kelompok]
  (db/get-data (str "select kodejur,jurusan,nm from pg1 where ptn='" funiv "' and
                                        nm<='" ntot "' and kelompok='" kelompok "' order by nm desc") 2))

(defn get-jurusan [ptn kelompok ntot]
  (db/get-data (str "select kodejur,jurusan,nm from pg1 where ptn='" ptn "' and
                                        nm<='" ntot "' and kelompok='" kelompok "' order by nm desc") 2))

(defn pilih-paket-ppdb []
  (db/get-data "select kode,kodemat,kodeipa,kodeind,kodeing,keterangan
               from simppdb order by keterangan" 2))

(defn paket-ppdb-all [kode]
  (db/get-data (str "select * from simppdb where kode='" kode "'") 1))

(defn nilaimat-smp [nis kodemat]
  (db/get-data (str "select nilai from datato where nis='" nis "' and kode='" kodemat "'") 1))

(defn nilaiipa-smp [nis kodeipa]
  (db/get-data (str "select nilai from datato where nis='" nis "' and kode='" kodeipa "'") 1))

(defn nilaiind-smp [nis kodeind]
  (db/get-data (str "select nilai from datato where nis='" nis "' and kode='" kodeind "'") 1))

(defn nilaiing-smp [nis kodeing]
  (db/get-data (str "select nilai from datato where nis='" nis "' and kode='" kodeing "'") 1))

(defn filter-daerah [ntot]
  (db/get-data (str "select distinct pgsma.kodedaerah as kode, daerah from pgsma
                    inner join kodedaerah on pgsma.kodedaerah=kodedaerah.kode
                    where nm <='" ntot "' order by daerah") 2))

(defn filter-sekolah [fkode ntot]
  (db/get-data (str "select sekolah,nm from pgsma where kodedaerah='" fkode "' and
                                        nm<='" ntot "' order by nm desc") 2))

(defn get-sekolah [kode ntot]
  (db/get-data (str "select sekolah,nm from pgsma where kodedaerah='" kode "' and
                                        nm<='" ntot "' order by nm desc") 2))
