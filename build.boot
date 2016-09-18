(set-env!
 :source-paths #{"src" "content" "css"}
 :dependencies '[[perun "0.3.0" :scope "test"]
                 [pandeiro/boot-http "0.7.0"]
                 [hiccup "1.0.5"]
                 [jeluard/boot-notify "0.2.1" :scope "test"]
                 [confetti/confetti "0.1.2-SNAPSHOT"]])

(require '[io.perun :as p]
         '[clojure.string :as string]
         '[confetti.boot-confetti :as confetti :refer [sync-bucket create-site]]
         '[pandeiro.boot-http :refer [serve]]
         '[jeluard.boot-notify :refer [notify]])

(defn permalink-fn [{:keys [slug path filename] :as post-data}]
  (if (.startsWith path "posts")
    (str "/posts/" slug ".html")
    (str (string/replace filename #"(?i).[a-z]+$" ".html"))))

(defn slug-fn [filename]
  (->> (string/split filename #"[-\.]")
    drop-last
    (string/join "-")
    string/lower-case))

(deftask build
  ""
  []
  (let [post? (fn [{:keys [path]}] (.startsWith path "posts"))]
    (comp
      (sift :move {#"basscss.min.css" "public/css/basscss.min.css"})
      (p/markdown)
      (p/slug :slug-fn slug-fn)
      (p/print-meta)
      (p/permalink :permalink-fn permalink-fn)
      (p/render :renderer 'site.core/page)
      (p/collection
        :renderer 'site.core/index
        :filterer post?
        :page "index.html"))))

(deftask dev
  ""
  []
  (comp (serve :resource-root "public")
        (watch)
        (build)
        (notify)))

(def confetti-edn
  (read-string (slurp "log-jelle-io.confetti.edn")))

(deftask deploy []
  (comp
    (build)
    (sift :include #{#"^public/"})
    (sift :move {#"^public/" ""})
    (sync-bucket :bucket (:bucket-name confetti-edn)
      :prune true
      :cloudfront-id (:cloudfront-id confetti-edn)
      :access-key (:access-key confetti-edn)
      :secret-key (:secret-key confetti-edn))))
