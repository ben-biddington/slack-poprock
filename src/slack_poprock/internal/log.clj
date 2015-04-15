(ns slack-poprock.internal.log)

(defn- now[] (new java.util.Date))
(defn info[fmt & args]
  (let [msg (apply format fmt args)]
    (println (format "[%s] %s" (now) msg))))
