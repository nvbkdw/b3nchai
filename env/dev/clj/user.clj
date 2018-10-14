(ns user
  (:require [bench-ai.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [bench-ai.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'bench-ai.core/repl-server))

(defn stop []
  (mount/stop-except #'bench-ai.core/repl-server))

(defn restart []
  (stop)
  (start))


