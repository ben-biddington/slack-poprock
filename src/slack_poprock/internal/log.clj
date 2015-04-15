(ns slack-poprock.internal.log)

(def ^{:private true} l (atom (fn[msg])))
(defn- now[] (new java.util.Date))
(defn info[msg] (println (format "[%s] %s" (now) msg)))
