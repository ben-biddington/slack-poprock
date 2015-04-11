(ns slack-poprock.core
  (:gen-class) 
  (:require [clojure.data.json :as json]
            [manifold.stream :as s]))

(require '[aleph.http :as http])
(require '[clj-http.client :as client])
(require '[slack-poprock.human :as poprock])

(def token      (System/getenv "TOKEN")) ;; => https://trapslinger.slack.com/services/4345477538?icon=1
(def start-url  (str "https://slack.com/api/rtm.start?token=" token))

(def current-message-id (atom 0))
(defn- next-id[] (swap! current-message-id inc))
(defn- deserialize[what] (json/read-str what :key-fn keyword))
(defn- slack-start-info[] (deserialize (:body (client/get start-url))))

(defn- slack-start-url[] (:url (slack-start-info)))
(defn- connect-to[url] (aleph.http/websocket-client url))
(def c @(connect-to (slack-start-url)))
(def slack-channels (:channels (slack-start-info))) 
(defn i[msg] (println (format "[%s] %s" (new java.util.Date) msg)))  

(defn- send[what]
  (i (format ">>> %s" what))
  (s/put! c (json/write-str (merge {:id (next-id)} what))))

(defn- slack-adapter[channel,text] (send {:type "message" :channel channel :text text}))

(defn- listen[text]
  (let [msg (deserialize text)] 
    (i (format "<<< %s" msg))
    (poprock/reply msg)))

(defn start[] 
  (poprock/log-with i)
  (poprock/reply-with slack-adapter)
  (s/consume listen c))

(defn- ping[] (send { :type "ping" })) ;; https://api.slack.com/rtm

;; (@reply-with (:channel to) (rand-nth replies))

(defn -main 
  [& args]
  (println (format "Starting Slack Poprock with args: <%s>" (if (nil? args) "none" args)))
  (start)
  (while true
    (Thread/sleep (* 45 1000))
    (ping)))

;; Realtime API => https://api.slack.com/rtm
;; Emoticons    => http://www.emoji-cheat-sheet.com/
