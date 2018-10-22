(ns bench-ai.evaluation
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as response]
            [bench-ai.config :refer [env]])
  (:import (io.reactivex.processors ReplayProcessor)
           (bench.ai.transcribe EvaluationStart AWSTranscribeCaller JobPoller ASREvaluation)
           (io.reactivex Flowable)
           (io.reactivex.schedulers Schedulers)
           (com.amazonaws.services.transcribe AmazonTranscribeClient)
           ))

(defn test [name] (log/info "Get called ?"))

(defn benchmark [filename]
  (log/info "Is this getting hit??")
  (let [client (-> (AmazonTranscribeClient/builder) (.build))
        processor (ReplayProcessor/create)]
          (-> (EvaluationStart/transform processor)
              (.observeOn (Schedulers/io))
              (AWSTranscribeCaller/transform client)
              (.observeOn (Schedulers/computation))
              (JobPoller/transform)
              (ASREvaluation/transform)
              (Flowable/fromPublisher)
              (.subscribe)
              ;(.subscribe (fn [] ())                        ; onNext
              ;            (fn [throwable] (log/error (.getMessage throwable))) ; onError
              ;            (fn [] ())        ; onComplete
              ;            )
              )
          (.onNext processor (EvaluationStart/getJob filename))
    )
  )
