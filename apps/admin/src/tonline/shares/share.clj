(ns tonline.shares.share)

(defn create-kode [nchar]
  (let [vc (vec "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
        cvc (count vc)]
    (loop [i 0 x []]
      (if (= i nchar)
        (apply str x)
        (recur (inc i)
               (conj x (rand-nth vc)))))))
