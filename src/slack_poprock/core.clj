(ns slack-poprock.core
  (:gen-class) 
  (:refer-clojure :exclude [send])
  (:require [clojure.data.json :as json]
            [manifold.stream :as s]
            [slack-poprock.slack :refer :all :as slack]))

(require '[aleph.http :as http])
(require '[clj-http.client :as client])
(require '[slack-poprock.human :as poprock])

(def token      (or (System/getenv "TOKEN") (slurp ".token"))) ;; => https://trapslinger.slack.com/services/4345477538?icon=1
(def start-url  (str "https://slack.com/api/rtm.start?token=" token))

(def current-message-id (atom 0))
(defn- next-id[] (swap! current-message-id inc))
(defn- deserialize[what] (json/read-str what :key-fn keyword))
(defn slack-start-info-raw[] 
  (deserialize (:body (client/get start-url))))

(def slack-start-info (memoize slack-start-info-raw))

(defn- slack-start-url[] (:url (slack-start-info)))
(defn- connect-to[url] (aleph.http/websocket-client url))
(def c @(connect-to (slack-start-url)))
(def slack-channels (:channels (slack-start-info)))
(defn- now[] (new java.util.Date))
(defn i[msg] (println (format "[%s] %s" (now) msg)))  

(defn- send[what]
  (i (format ">>> %s" what))
  (s/put! c (json/write-str (merge {:id (next-id)} what))))

(defn- slack-adapter[channel,text] (send {:type "message" :channel channel :text text}))

(def slack-settings {:users (slack/users token)})

(defn- listen[text]
  (let [msg (deserialize text)] 
    (i (format "<<< %s" msg))
    (poprock/reply msg slack-settings)))

(defn start[] 
  (poprock/log-with i)
  (poprock/reply-with slack-adapter)
  (s/consume listen c))

(defn- ping[] (send { :type "ping" })) ;; https://api.slack.com/rtm

(defn -main 
  [& args]
  (println (format "Starting Slack Poprock with args: <%s>" (if (nil? args) "none" args)))
  (start)
  (while true
    (Thread/sleep (* 45 1000))
    (ping)))

;; Realtime API => https://api.slack.com/rtm
;; Emoticons    => http://www.emoji-cheat-sheet.com/
