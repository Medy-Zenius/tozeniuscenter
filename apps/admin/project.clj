(defproject tonline "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [lib-noir "0.9.9"]
                 [selmer "1.11.2"]
                 [ring-server "0.5.0"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [dk.ative/docjure "1.11.0"]
                 [org.clojure/data.json "0.2.6"]]
  :repl-options {:init-ns tonline.repl
                 :timeout 1200000}
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler tonline.handler/app
         :init tonline.handler/init
         :destroy tonline.handler/destroy
         :port 30981
         ;:port 5000
         }
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}})
