(defproject clogal "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.2"]
                 [ring/ring-defaults "0.2.3"]
                 [ring/ring-jetty-adapter "1.5.0"]  ;; this is only needed for my -main
				 [hiccup "1.0.5"]
                 [ring-basic-authentication "1.0.5"]
                 [digest "1.4.5"]
                 [org.clojure/tools.cli "0.3.5"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler clogal.handler/app :port 3333}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.0"]]}}
  :main clogal.core)
