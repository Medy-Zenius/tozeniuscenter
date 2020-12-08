(ns tonline.models.getdata
  (:require [tonline.models.db :as db]))

(defn nis-ada [vnis]
  (db/get-data (str "select nis from users where nis='" vnis "'") 1))

(defn admin [id]
  (db/get-data (str "select * from admin where id='" id "'") 1))

(defn users-all [nis]
  (db/get-data (str "select * from users where nis='" nis "'") 1))

(defn kategori []
  (db/get-data "select * from kategori order by nama" 2))

(defn simppdb-all []
  (db/get-data "select * from simppdb order by keterangan" 2))

(defn simppdb-kode [kode]
  (db/get-data (str "select * from simppdb where kode='" kode "'") 1))

(defn simppdb-peserta [kmat kipa kind king npsn kelas]
  (db/get-data (str "select distinct datato.nis as nis,name,npsn,kelas from datato
                                 inner join users on datato.nis=users.nis where
                                 (kode='" kmat "' or kode='" kipa "' or
                                 kode='" kind "' or kode='" king "')
                                 and users.npsn like '" npsn "%'
                                 and users.kelas like '" kelas "%'") 2))

(defn simppdb-nilai [kode npsn kelas]
  (db/get-data (str "select datato.nis as nis,jawaban,nilai from datato inner join users
                     on datato.nis=users.nis where datato.kode='" kode "'
                     and users.npsn like '" npsn "%' and users.kelas like '" kelas "%'") 2))

(defn simppdb-kodepaket [kode]
  (db/get-data (str "select kodemat,kodeipa,kodeind,kodeing from simppdb where kode='" kode "'") 1))

(defn simsbmptn-all []
  (db/get-data "select * from simsbmptn order by keterangan" 2))

(defn simsbmptn [kode]
  (db/get-data (str "select * from simsbmptn where kode='" kode "'") 1))

(defn sbmptn-peserta [ktkpa kipaorips npsn kelas]
  (db/get-data (str "select distinct datato.nis as nis,name,npsn,kelas from datato
                                 inner join users on datato.nis=users.nis where
                                 (kode='" ktkpa "' or kode='" kipaorips "')
                                 and users.npsn like '" npsn "%'
                                 and users.kelas like '" kelas "%'") 2))

(defn nama-kat [kode]
  (:nama (db/get-data  (str "select nama from kategori where kode='" kode "'") 1)))

(defn kategori-proset [kkode ket]
  (db/get-data (str "select proset.kode as kode,nama,keterangan,jsoal,waktu,status from proset
                               inner join kategori on kategori.kode=proset.kodekategori where
                               proset.kodekategori='" kkode "' and upper(keterangan) LIKE '%" ket "%'
                               order by keterangan") 2))

(defn proset-edit [kode]
  (db/get-data (str "select golongan,kunci,jsoal,jumpil,jenis,upto,pretext,sound,keterangan from proset
                    where kode='" kode "'") 1))

(defn status-kodex [kode]
  (db/get-data (str "select status,kodex,jsoal,keterangan from proset where kode='" kode "'") 1))

(defn proset-all [kode]
  (db/get-data (str "select * from proset where kode='" kode "'") 1))

(defn kunci [kode]
  (db/get-data (str "select kunci from proset where kode='" kode "'") 1))

(defn state-acak [kode]
  (db/get-data (str "select kode,acak,polaacak,keterangan from proset where kode='" kode "'") 1))

(defn aktif-soal [kode]
  (db/get-data (str "select kode,kodekategori,status,keterangan from proset where kode='" kode "'") 1))

(defn justonce [kode]
  (db/get-data (str "select kode,kodekategori,justonce,keterangan from proset where kode='" kode "'") 1))

(defn constant [nama]
  (db/get-data (str "select value from constants where nama='" nama "'") 1))

(defn mdata-proset [kode]
  (db/get-data (str "select keterangan,jsoal,kunci from proset where kode='" kode "'") 1))

(defn hasil-proset [kode npsn kelas max-row offset]
  (db/get-data (str "select name,users.nis as nis,npsn,kelas,nilai,jawaban from users INNER JOIN datato
                   ON users.nis=datato.nis where kode='" kode "' and
                    npsn LIKE '" npsn "%' and kelas LIKE '" kelas "%' order by nilai desc
                    LIMIT " max-row " OFFSET " offset) 2))

(defn total-hasil-proset [kode npsn kelas]
  (db/get-data (str "select count(*) as total from users INNER JOIN datato
                    ON users.nis=datato.nis where datato.kode='" kode "' and
                    users.npsn LIKE '" npsn "%' and kelas LIKE '" kelas "%'") 1))

(defn jumlah-nama [unama nis]
  (db/get-data (str "select count(*) as jumlah from users where upper(name) LIKE '%" unama "%'
                    and nis LIKE '" nis "%'") 1))

(defn search-nama [unama nis vorder max-row voffset]
  (db/get-data (str "select nis,name,email,npsn from users where upper(name) LIKE '%" unama "%'
                     and nis LIKE '" nis "%' order by " vorder " LIMIT " max-row " OFFSET " voffset) 2))

(defn sekolah [npsn nama max-row]
  (db/get-data (str "select * from sekolah where npsn LIKE '%" npsn "%' and upper(nama) LIKE '%" nama "%'
               order by npsn,nama LIMIT " max-row) 2))

(defn sekolah-npsn [npsn]
  (db/get-data (str "select * from sekolah where npsn='" npsn "'") 1))

(defn daerah []
  (db/get-data "select * from kodedaerah order by daerah asc" 2))

(defn pgsma-nama [nama]
  (db/get-data (str "select * from pgsma where upper(sekolah) LIKE '%" nama "%' order
                    by kodedaerah,sekolah") 2))

(defn sekolah-pgsma [nomer]
  (db/get-data (str "select * from pgsma where nomer='" nomer "'") 1))

(defn daerah-nama [nama]
  (db/get-data (str "select * from kodedaerah where upper(daerah)
                          LIKE '%" nama "%' order by daerah") 2))

(defn kodedaerah [kode]
  (db/get-data (str "select * from kodedaerah where kode='" kode "'") 1))

(defn datato-nis-jawaban [kode]
  (db/get-data (str "select nis,jawaban from datato where kode='" kode "'") 2))

(defn datato-jawaban [kode]
  (db/get-data (str "select jawaban from datato where kode='" kode "'") 2))

(defn datato-jawaban-tk [kode]
  (db/get-data (str "select jawaban, nilai from datato
                                   where kode='" kode "' order by nilai desc") 2))
