(ns slack-poprock.core
  (:gen-class)
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [manifold.stream :as streams]))

(require '[aleph.http :as http])
(require '[clj-http.client :as client])

(def user-name "@U04B4FE2Y")

(def token (System/getenv "TOKEN")) ;; => https://trapslinger.slack.com/services/4345477538?icon=1
(def start-url (str "https://slack.com/api/rtm.start?token=" token))

(defn- slack-start-info[]
  (json/read-str (:body (client/get start-url)) :key-fn keyword))

(defn- slack-start-url[]
  (let [all (slack-start-info)]
    (:url all)))

(defn- connect-to[url] (aleph.http/websocket-client url))

(def c @(connect-to (slack-start-url)))

(def slack-channels
  (let [all (slack-start-info)]
    (:channels all))) 

(defn- mentioned-me[msg]
  (let [text (:text msg)]
    (if (clojure.string/blank? text) false (.contains text  user-name))))

(defn- reply-with[channel,text]
  ;;(streams/put! c "{\"id\": 1, \"type\": \"message\", \"channel\": \"C04AUKBH5\", \"text\": \"Hi everybody\"}") 
  (streams/put! c (json/write-str {:id 1 :type "message" :channel channel :text text}))
  (prn text))

(defn- reply[to]
  (when (mentioned-me to)
    (reply-with (:channel to) "Heck Mandy!")))

(defn- listen[message]
  (prn 'message! message)
  (reply (json/read-str message :key-fn keyword)))

(defn start[] (streams/consume listen c))

(defn -main
  [& args]
  (println "Starting...")
  (start)
  (while true
    (Thread/sleep 1000)))

;; Realtime API => https://api.slack.com/rtm
