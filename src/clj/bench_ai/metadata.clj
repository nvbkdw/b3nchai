(ns bench-ai.metadata
  (:require [clojure.java.jdbc :as sql])
  (:import (java.util UUID)))

(def db {:subprotocol "postgresql"
         :subname     "//dumtest-cluster.cluster-cuugh3aso3rn.us-west-2.rds.amazonaws.com/metadata"
         :user        "poweruser"
         :password    "password"}
  )

(defn uuid [] (UUID/randomUUID))

(defn list-tasks []
  (sql/query db ["select * from asr_tasks"]))

(defn insert-asr-task! [task]
  (sql/insert! db :asr_tasks task))

(defn get-vendor-id [name]
  ((first
     (sql/query db ["select id from vendors where name = ?" name])
     )
    :id)
  )

; persist evaluation task result in database
(defn update-task-result [id vendorName wer ins del sub s3Path]
                    (insert-asr-task! {:id id
                                       :vendor (get-vendor-id vendorName)
                                       :result_s3_location s3Path
                                       :wer wer
                                       :insertion ins
                                       :deletion del
                                       :substitution sub
                                       }))


;(update-task-result (uuid) "AmazonTranscribe" 1 0 0 0 "s3Path")




