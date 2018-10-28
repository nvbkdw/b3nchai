(ns bench-ai.config
  (:require [cprop.core :refer [load-config]]
            [cprop.source :as source]
            [mount.core :refer [args defstate]])
  (:import (io.reactivex.processors ReplayProcessor)))

(defstate env
          :start
          (load-config
            :merge
            [(args)
             (source/from-system-props)
             (source/from-env)]))
