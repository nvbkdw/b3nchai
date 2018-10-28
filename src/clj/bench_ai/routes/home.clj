(ns bench-ai.routes.home
  (:require [bench-ai.layout :as layout]
            [bench-ai.db.core :as db]
            [compojure.core :refer [defroutes GET POST PUT]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [bench-ai.config :refer [env]]
            [amazonica.aws.s3 :as s3]
            [cheshire.core :refer [generate-string]]
            [bench-ai.evaluation :as evaluation]))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defn s3-sign [filename contentType]
  (let [presigned-url (s3/generate-presigned-url {:access-key (:aws-access-key env)
                                                  :secret-key (:aws-secret-key env)
                                                  :endpoint   (:aws-region env)}
                                                 (:aws-bucket env)
                                                 filename
                                                 (t/plus (t/now) (t/minutes 5))
                                                 "PUT")]
    (response/created (str presigned-url) (generate-string {:method  "PUT"
                                                            :url     (str presigned-url)
                                                            :fields  {}
                                                            :headers {}}))))



(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))
  (POST "/s3-sign" [filename contentType] (s3-sign filename contentType))
  (POST "/benchmark" [filename] (evaluation/benchmark filename)))

