(ns user
  (:require [bench-ai.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [bench-ai.core :refer [start-app]]
            [bench-ai.db.core]
            [conman.core :as conman]
            [luminus-migrations.core :as migrations]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'bench-ai.core/repl-server))

(defn stop []
  (mount/stop-except #'bench-ai.core/repl-server))

(defn restart []
  (stop)
  (start))

(defn restart-db []
  (mount/stop #'bench-ai.db.core/*db*)
  (mount/start #'bench-ai.db.core/*db*)
  (binding [*ns* 'bench-ai.db.core]
    (conman/bind-connection bench-ai.db.core/*db* "sql/queries.sql")))

(defn reset-db []
  (migrations/migrate ["reset"] (select-keys env [:database-url])))

(defn migrate []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration [name]
  (migrations/create name (select-keys env [:database-url])))


