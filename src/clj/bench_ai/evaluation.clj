(ns bench-ai.evaluation
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :as response]
            [bench-ai.config :refer [env]])
  (:import (io.reactivex.processors ReplayProcessor)
           (bench.ai.transcribe EvaluationStart AWSTranscribeCaller JobPoller ASREvaluation)
           (io.reactivex Flowable)
           (io.reactivex.schedulers Schedulers)
           (com.amazonaws.services.transcribe AmazonTranscribeClient)
           (com.amazonaws.auth AWSStaticCredentialsProvider BasicAWSCredentials)))

(defn aws-client []
  (let [basic-creds (BasicAWSCredentials. (env :aws-access-key) (env :aws-secret-key))
        builder (-> (AmazonTranscribeClient/builder)
                    (.withRegion (env :aws-region)))]
    (.setCredentials builder (AWSStaticCredentialsProvider. basic-creds))
    (.build builder)))

(defn benchmark [filename]
  (let [processor (ReplayProcessor/create)]
          (-> (EvaluationStart/transform processor)
              (.observeOn (Schedulers/io))
              (AWSTranscribeCaller/transform (aws-client))
              (.observeOn (Schedulers/computation))
              (JobPoller/transform (aws-client))
              (ASREvaluation/transform)
              (Flowable/fromPublisher)
              (.subscribe)
              ;(.subscribe (fn [] ())                        ; onNext
              ;            (fn [throwable] (log/error (.getMessage throwable))) ; onError
              ;            (fn [] ())        ; onComplete
              ;            )
              )
          (.onNext processor (EvaluationStart/getJob filename (env :aws-bucket)))
          (response/created (env :url))
    )
  )
