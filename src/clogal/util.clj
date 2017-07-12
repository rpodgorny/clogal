(ns clogal.util
  (:require [clojure.string]
            [clojure.java.shell]
            [clojure.java.io]
            [ring.adapter.jetty]
            [digest :refer [sha-256]]))

(defn is-dir? [f] (.isDirectory f))

(defn get-dirs [f]
  (sort #(compare %1 %2) (filter is-dir? (.listFiles f))))

(def image-exts #{".jpeg" ".jpg" ".gif" ".png"})

(defn is-image? [fn]
  (and (.isFile fn)
       (some #(clojure.string/ends-with? (clojure.string/lower-case fn) %)
             image-exts)))

(defn get-images [f]
  (sort #(compare %1 %2) (filter is-image? (.listFiles f))))

(defn img-adv [fn dec-or-inc]
  (let [f (clojure.java.io/file fn)
        parent-f (.getParentFile f)
        files (map #(.getName %) (get-images parent-f))
        idx (dec-or-inc (.indexOf files (.getName f)))]
    (if (and (>= idx 0) (< idx (count files)))
      (nth files idx))))

(defn to-thumb-path [size f thumb-root]
  (str thumb-root "/" size "/" (digest/sha-256 f)))

(defn make-thumb [size f thumb-root]
  (let [thumb-f (clojure.java.io/file (to-thumb-path size f thumb-root))]
    (if-not (.isDirectory (.getParentFile thumb-f))
      (.mkdirs (.getParentFile thumb-f)))
    (clojure.java.shell/sh "convert" (str f) "-resize" (str size "x" size) (str thumb-f))
    thumb-f))

(defn dir-name-old [fn]
  (-> (clojure.string/split fn #"/")
      (butlast)
      (clojure.string/join "/")))

(defn dir-name [fn]
  (clojure.string/replace fn #"(.*)/(.*)" "$1/"))
