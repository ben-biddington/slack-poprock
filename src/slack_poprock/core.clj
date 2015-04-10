(ns slack-poprock.core
  (:gen-class)
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [manifold.stream :as streams]))

(require '[aleph.http :as http])
(require '[clj-http.client :as client])

(def replies
  [
    "Heck Mandy!"
    "Ma balls!" 
    "Mate! I've just had a shower!"
    "Rotten dog! :dog:"
    "Chuck it on the fire! :fire:"
    "Who's farting? :boom:"
    "Crikey dick!"
    "Goh-lee!"
    "Mol!"
    "Col!"
    "Joss-ette!"])

(def user-name "@U04B4FE2Y")
(def nick-name "poppo")
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

(defn- mentioned[text,what]
  (if (clojure.string/blank? text) false (.contains text what)))

(defn- mentioned-me[msg]
  (or (mentioned (:text msg) user-name) (mentioned (:text msg) nick-name)))

(defn- mentioned-chocolate[msg]
  (mentioned (:text msg) "chocolate"))

(defn- reply-with[channel,text]
  (streams/put! c (json/write-str {:id 1 :type "message" :channel channel :text text}))
  (prn text))

(defn- reply[to]
  (when (mentioned-me to)
    (reply-with (:channel to) (rand-nth replies)))

  (when (mentioned-chocolate to)
    (reply-with (:channel to) "Did someone say chocolate?")))

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
;; Emoticons -> http://www.emoji-cheat-sheet.com/
