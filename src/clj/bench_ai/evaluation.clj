(ns bench-ai.evaluation
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :as response]
            [bench-ai.metadata :as metadata]
            [bench-ai.config :refer [env]]
            [mount.core :refer [args defstate]])
  (:import (io.reactivex.processors ReplayProcessor)
           (bench.ai.transcribe EvaluationStart AWSTranscribeCaller JobPoller ASREvaluation S3Uploader)
           (io.reactivex Flowable)
           (io.reactivex.schedulers Schedulers)
           (io.reactivex.functions Consumer)
           (com.amazonaws.auth AWSStaticCredentialsProvider BasicAWSCredentials)
           (com.amazonaws.services.transcribe AmazonTranscribeClient)
           (com.amazonaws.services.s3 AmazonS3Client)))

(def processor (ReplayProcessor/create))

(defn createAwsCredsProvider []
  (let [basic-creds (BasicAWSCredentials. (env :aws-access-key) (env :aws-secret-key))]
    (AWSStaticCredentialsProvider. basic-creds)
    ))

(defn createAmazonTranscribeClient [credsProvider]
  (let [builder (-> (AmazonTranscribeClient/builder)
                    (.withRegion (env :aws-region)))]
    (.setCredentials builder credsProvider)
    (.build builder)))

(defn createS3Client [credsProvider]
  (let [builder (-> (AmazonS3Client/builder)
                    (.withRegion (env :aws-region)))]
    (.setCredentials builder credsProvider)
    (.build builder)))

(defn persist-metadata []
  (reify
    Consumer
    (accept [this task]
      (metadata/update-task-result (metadata/uuid) "AmazonTranscribe" (.getWer task) (.getIns task)  (.getDel task) (.getSub task) (.getS3Path task))
      )))


; start workflow
;(->
;  (EvaluationStart/transform processor)
;  (.observeOn (Schedulers/io))
;  (AWSTranscribeCaller/transform amazonTranscribeClient)
;  (.observeOn (Schedulers/computation))
;  (JobPoller/transform amazonTranscribeClient)
;  (ASREvaluation/transform)
;  (S3Uploader/transform s3Client (env :aws-bucket))
;  (Flowable/fromPublisher)
;  (.doOnNext (persist-metadata)) ; write result to database
;  (.subscribe)
;  )

(defn benchmark [filename]
  ; start evaluation flow
  (log/info (str "Start ASR evaluation task: " filename)
  (.onNext processor (EvaluationStart/getJob filename (env :aws-bucket)))
  (response/created (env :url))
  )

(defstate evaluation
          :start
          (let [credsProvider (createAwsCredsProvider)
                amazonTranscribeClient (createAmazonTranscribeClient credsProvider)
                s3Client (createS3Client credsProvider)]
          (->
            (EvaluationStart/transform processor)
            (.observeOn (Schedulers/io))
            (AWSTranscribeCaller/transform amazonTranscribeClient)
            (.observeOn (Schedulers/computation))
            (JobPoller/transform amazonTranscribeClient)
            (ASREvaluation/transform)
            (S3Uploader/transform s3Client (env :aws-bucket))
            (Flowable/fromPublisher)
            (.doOnNext (persist-metadata)) ; write result to database
            (.subscribe)
            ))
          :stop
          (.onComplete processor))
