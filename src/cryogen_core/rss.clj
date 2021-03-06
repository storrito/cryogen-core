(ns cryogen-core.rss
  (:require [clj-rss.core :as rss]
            [text-decoration.core :refer :all]
            [cryogen-core.io :as cryogen-io]
            [cryogen-core.util :as cryogen-util])
  (:import java.util.Date))


(defn posts-to-items [{:keys [site-url blog-prefix]} posts]
  (map
    (fn [{:keys [uri title content-dom date enclosure author description image]}]
      (let [site-url (if (.endsWith site-url "/") (apply str (butlast site-url)) site-url)
            link (str site-url uri)]
        (cond-> {:guid        link
                 :link        link
                 :title       title
                 :description description
                 :author      author
                 :pubDate     date}
                image
                (assoc :thumbnail (str site-url blog-prefix image))
                enclosure
                (assoc :enclosure enclosure))))
    posts))

(defn make-channel [config posts]
  (apply
    (partial rss/channel-xml
             false
             {:title         (:site-title config)
              :link          (:site-url config)
              :description   (:description config)
              :lastBuildDate (Date.)})
    (posts-to-items config posts)))

(defn make-filtered-channels [{:keys [rss-filters blog-prefix] :as config} posts-by-tag]
  (doseq [filter rss-filters]
    (let [uri (cryogen-io/path "/" blog-prefix (str (name filter) ".xml"))]
      (println "\t-->" (cyan uri))
      (cryogen-io/create-file uri (make-channel config (get posts-by-tag filter))))))
