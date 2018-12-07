
(defproject cbtonline "0.1.0-SNAPSHOT"
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
                 [com.draines/postal "2.0.2"]]
  :repl-options {:init-ns cbtonline.repl
                 :timeout 1200000}
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler cbtonline.handler/app
         :init cbtonline.handler/init
         :destroy cbtonline.handler/destroy
         :port 18559
         ;:port 5000
         }
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}})
