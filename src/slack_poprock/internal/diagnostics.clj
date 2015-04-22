(ns slack-poprock.internal.diagnostics
  (:refer-clojure :exclude [time])
  (:require [clj-time.core :as t]))

(defn time[f & args]
  (let [start (t/now)]
    (let [result (apply f args)]
      { 
       :duration (t/interval start (t/now)) 
       :result result })))
