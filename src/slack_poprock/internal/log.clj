(ns slack-poprock.internal.log)

(defn- now[] (new java.util.Date))
(defn info[fmt & args]
  (let [msg (String/format fmt (to-array args))]
    (println (format "[%s] %s" (now) msg))))
