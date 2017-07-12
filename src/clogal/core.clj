(ns clogal.core
  (:gen-class)
  (:require [clogal.util :refer :all]
            [clogal.handler :refer :all]
            [ring.adapter.jetty]
            [clojure.tools.cli :refer [parse-opts]]))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 3333
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-d" "--thumb-dir DIR" "Where to store thumbnails"]
   ["-h" "--help" "Help"]])

(defn -main [& args]
  (let [parsed-args (parse-opts args cli-options)
        port (get-in parsed-args [:options :port])
        dir (first (:arguments parsed-args))
        thumb-dir (get-in parsed-args [:options :thumb-dir])
        app2 (create-app dir thumb-dir)]
    (println "args:" args "dir:" dir)
    (println "parsed-args:" parsed-args)
    (ring.adapter.jetty/run-jetty app2 {:port port})))  ;; TODO
