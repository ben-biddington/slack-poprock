(ns slack-poprock.core
  (:gen-class)
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [manifold.stream :as s]))

(require '[aleph.http :as http])
(require '[clj-http.client :as client])

(def replies
  [
    "Heck Mandy!"
    "Ma balls!"
    "Timm-o!"
    "Mate! I've just had a shower!"
    "Rotten dog! :dog:"
    "Chuck it on the fire! :fire:"
    "Who's farting? :boom:"
    "Crikey dick!"
    "Goh-lee!"
    "Did you say arse lick?"
    "Mol!"
    "Col!"
    "Jimm-eh!"
    "Who's Rizz?"
    "Joss-ette!"])

(def user-name "@U04B4FE2Y")
(def nick-names ["poppo" "rick" "p-rick" "ricky" "mark wigg"])
(def token (System/getenv "TOKEN")) ;; => https://trapslinger.slack.com/services/4345477538?icon=1
(def start-url (str "https://slack.com/api/rtm.start?token=" token))

(def current-message-id (atom 0))
(defn- next-id[] (swap! current-message-id inc))

(defn- slack-start-info[]
  (json/read-str (:body (client/get start-url)) :key-fn keyword))

(defn- slack-start-url[]
  (let [all (slack-start-info)]
    (:url all)))

(defn- connect-to[url] (aleph.http/websocket-client url))

(def c @(connect-to (slack-start-url)))

(def slack-channels (:channels (slack-start-info))) 

(defn- mentioned[text what]
  (if (clojure.string/blank? text) false (.contains (.toLowerCase text) (.toLowerCase what))))

(defn- mentioned-me[msg]
  (or
   (mentioned (:text msg) user-name) 
   (some #(mentioned (:text msg) %) nick-names)))

(defn- mentioned-chocolate[msg]
  (mentioned (:text msg) "chocolate"))

(defn- message?[msg]
  (let [type (:type msg)]
    (if (nil? type) false (= "message" type))))

(defn- dm?[msg]
  (let [channel (:channel msg)]
    (and 
     (message? msg) 
     (if (nil? channel) false (= "D04B4FE3E" channel)))))

(defn- send[what]
  (s/put! c (json/write-str (merge {:id (next-id)} what))))

(defn- reply-with[channel,text]
  (send {:type "message" :channel channel :text text})
  (prn text))

(defn- reply[to]
  (when (or (mentioned-me to) (dm? to))
    (reply-with (:channel to) (rand-nth replies)))

  (when (mentioned-chocolate to)
    (reply-with (:channel to) "Did someone say chocolate?")))

(defn- listen[message]
  (prn 'message! message)
  (reply (json/read-str message :key-fn keyword)))

(defn start[] (s/consume listen c))

(defn- now [] (new java.util.Date))

(defn- ping[] ;; https://api.slack.com/rtm
  (send { :type "ping" }))

(defn -main
  [& args]
  (println "Starting Slack Poprock with args: " args)
  (start)
  (while true
    (Thread/sleep (* 30 1000))
    (ping)
    (println "[" (str(now)) "] Connected?:" (if-not (s/closed? c) "YES" "NO"))))

;; Realtime API => https://api.slack.com/rtm
;; Emoticons -> http://www.emoji-cheat-sheet.com/
