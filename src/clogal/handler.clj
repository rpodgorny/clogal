(ns clogal.handler
  (:require [clojure.java.io]
            [clogal.util :refer :all]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [hiccup.core :refer :all]
            [ring.middleware.basic-authentication :refer [wrap-basic-authentication]]))

(defn listing [request]
  (let [path (get-in request [:params :path])
        root (:root request)
        fp (clojure.java.io/file (str root path))]
    (html [:html
           [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
           [:link {:rel "stylesheet" :href "/w3.css"}] ; https://www.w3schools.com/w3css/4/w3.css
           [:body
            [:div {:class "w3-bar w3-teal"}
             [:a {:href ".." :class "w3-bar-item w3-button" :style "width:100%"}
              "up"]]
            (when fp
              [:div {:class "w3-container"}
               (for [f (get-dirs fp)]
                 [:a {:href (str "/list" path (.getName f) "/") :class "w3-button"}
                  (.getName f)])])
            (when fp
              (for [f (get-images fp)]
                [:a {:href (str "/img" path (.getName f))}
                 [:img {:src (str "/thumb/200" path (.getName f))
                        :style "max-width: 200px; max-height: 200px"}]]))]])))

(defn img [request]
  (let [path (get-in request [:params :path])
        root (:root request)
        f (clojure.java.io/file (str root path))
        up (dir-name path)
        prev (img-adv f dec)
        next (img-adv f inc)]
    (html [:html
           [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
           [:link {:rel "stylesheet" :href "/w3.css"}]
           [:body
            [:div {:class "w3-bar w3-teal"}
             [:a {:href (if prev (str "/img" (dir-name path) prev) "#")
                  :class "w3-bar-item w3-button"
                  :style "width:33%"}
              "prev"]
             [:a {:href (str "/list" up)
                  :class "w3-bar-item w3-button"
                  :style "width:34%"}
              "up"]
             [:a {:href (if next (str "/img" (dir-name path) next) "#")
                  :class "w3-bar-item w3-button"
                  :style "width:33%"}
              "next"]]
            [:img {:src (str "/thumb/600" path)
                   :style "width:100%; max-width:100%; max-height:100%"}]
            [:a {:href (str "/raw" path) :class "w3-button w3-block"}
             "full size"]]])))

(defn thumb [request]
  (let [size (get-in request [:params :size])
        path (get-in request [:params :path])
        root (:root request)
        thumb-root (:thumb-root request)
        f (clojure.java.io/file (str root path))
        thumb-f (clojure.java.io/file (to-thumb-path size f thumb-root))]
    {:body (if (.isFile thumb-f)
             thumb-f
             (make-thumb size f thumb-root))}))

(defn authenticated? [name pass]
  (= name pass "atx"))

(defn make-app-routes [root]
  (list
    (GET "/" [] (html [:a {:href "/list/"} "list"]))
    (GET "/list:path{.*}" [] listing)
    (GET "/img:path{.*}" [] img)
    (GET "/thumb/:size{[0-9]*}:path{/.*}" [] thumb)
    (route/files "/raw" {:root root :allow-symlinks? true})
    (route/not-found "Not Found")))

(defn def-app-routes [root]
  (defroutes app-routes
    (GET "/" [] (html [:a {:href "/list/"} "list"]))
    (GET "/list:path{.*}" [] listing)
    (GET "/img:path{.*}" [] img)
    (GET "/thumb/:size{[0-9]*}:path{/.*}" [] thumb)
    (route/files "/raw" {:root root :allow-symlinks? true})
    (route/not-found "Not Found"))
  app-routes)

(defn wrap-my [handler root thumb-root]
  (fn [request]
    (handler (assoc (assoc request :root root) :thumb-root thumb-root))))

(defn create-app [root thumb-root]
  (-> (wrap-defaults (def-app-routes root) site-defaults)
      (wrap-basic-authentication authenticated?)
      (wrap-my root thumb-root)))
