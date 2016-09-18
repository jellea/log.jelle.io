(ns site.core
  (:require [hiccup.page :as hp]
            [clojure.string :as string])
  (:import java.text.SimpleDateFormat))

(defn date-fmt [date]
  (.format (java.text.SimpleDateFormat. "d MMMM yyyy") date))

(defn render-post [post]
  [:article.mb4
   [:a.h2 {:href (:permalink post) :alt (str (:title post) " permalink page")}
    (:title post)]
   (when (:date-published post)
     [:h6 (date-fmt (:date-published post))])
   [:div.content (:content post)]
   (when-let [clink (:clyp-id post)]
     [:iframe.mt1 {:width "100%" :height "160"
                   :src (str "https://clyp.it/" clink "/widget")
                   :frameborder "0"}])
   (when (:instruments-used post)
     (let [insts (-> (:instruments-used post) (string/split #","))]
       [:div.mt2
        (for [inst insts]
          [:a.mr1.h6 {:href "#"} (string/trim inst)])]))])

(defn base [page]
  (hp/html5
    {}
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
     [:link {:type "text/css" :rel "stylesheet"
             :href "https://npmcdn.com/basscss@8.0.2/css/basscss.min.css"}]
     [:style "body {font-family: -apple-system, Helvetica, Arial, sans-serif;}"]]
    [:body.mt4
     [:div.fixed.top-0.mt4.ml4
       [:a.h1 {:href "/"} "audio log"]]
     page]))

(defn page [data]
  (base
   [:div {:style "max-width: 600px; margin: 40px auto;"}
    (render-post (-> data :entry))]))

(defn index [{:keys [entries]}]
  (let [[curr-page no-of-pages] (:page (first entries))]
    (base
      [:div.mx-auto {:style "max-width: 600px;"}
        (for [post entries]
          (render-post post))])))
        ;(if (> no-of-pages 1)
        ;  (list [:section {:id "pagination"}
        ;         (for [i (range no-of-pages)]
        ;           (if (= i curr-page)
        ;             [:strong (inc i)]
        ;             [:a {:href (str "/" (pagination-path i))} (inc i)]))]))))))
