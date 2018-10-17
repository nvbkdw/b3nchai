(ns bench-ai.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [bench-ai.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[bench-ai started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[bench-ai has shut down successfully]=-"))
   :middleware wrap-dev})
