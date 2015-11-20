(ns slack-poprock.slack-token
  (:require [clojure.java.io :as io]))
  
(def filename ".token")

(defn- disk[]
  (when (not (.exists (io/file filename)))
    (throw (Exception. (format "Expected a file at <%s> to contain slack token. Find that token at <https://trapslinger.slack.com/services/4378524098>." filename))))
  (slurp filename))

(def current      
  (or 
   (System/getenv "TOKEN") 
   (disk))) ;; => https://trapslinger.slack.com/services/4345477538?icon=1
