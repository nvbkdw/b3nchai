(ns bench-ai.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[bench-ai started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[bench-ai has shut down successfully]=-"))
   :middleware identity})
