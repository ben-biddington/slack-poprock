(ns slack-poprock.core
  (:gen-class) 
  (:require [clojure.data.json :as json]
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
    "Tidy up! Come on!"
    "How's your maths?"
    "Col!"
    "Bla bla bla, see you later guys"
    "Baby baby baby, light my way"
    "Jimm-eh!"
    "Who's Rizz?"
    "Hudddddrrrrrrr"
    "Joss-ette!"])

(def user-name  "@U04B4FE2Y")
(def nick-names ["poppo" "rick" "p-rick" "ricky" "mark wigg"])
(def foods      ["chocolate" "licorice" "chipth"])
(def token      (System/getenv "TOKEN")) ;; => https://trapslinger.slack.com/services/4345477538?icon=1
(def start-url  (str "https://slack.com/api/rtm.start?token=" token))

(def current-message-id (atom 0))
(defn- next-id[] (swap! current-message-id inc))

(defn- slack-start-info[]
  (json/read-str (:body (client/get start-url)) :key-fn keyword))

(defn- slack-start-url[] (:url (slack-start-info)))
(defn- connect-to[url] (aleph.http/websocket-client url))
(def c @(connect-to (slack-start-url)))
(def slack-channels (:channels (slack-start-info))) 
(defn- deserialize[what] (json/read-str what :key-fn keyword))
(defn- now [] (new java.util.Date))

(defn- mentioned?[text what]
  (and 
   (not (clojure.string/blank? text)) 
   (.contains (.toLowerCase text) (.toLowerCase what))))

(defn- mentioned-me?[msg]
  (or
   (mentioned? (:text msg) user-name) 
   (some #(mentioned? (:text msg) %) nick-names)))

(defn- mentioned-food?[msg]
  (some #(mentioned? (:text msg) %) foods))

(defn- which-food?[msg]
  (first (filter #(mentioned? (:text msg) %) foods)))

(defn- message?[msg]
  (let [type (:type msg)]
    (if (nil? type) false (= "message" type))))

(defn- dm?[msg]
  (let [channel (:channel msg)]
    (and 
     (message? msg) 
     (if (nil? channel) false (= "D04B4FE3E" channel)))))

(defn- i[msg] (println (format "[%s] %s" (now) msg))) 

(defn- send[what]
  (i (format ">>> %s" what))
  (s/put! c (json/write-str (merge {:id (next-id)} what))))

(defn- reply-with[channel,text] (send {:type "message" :channel channel :text text}))

(defn- reply[to]
  (when (or (mentioned-me? to) (dm? to))
    (reply-with (:channel to) (rand-nth replies)))

  (when (mentioned-food? to)
    (reply-with (:channel to) (str "Did someone say " (which-food? to) "?"))))

(defn- listen[text]
  (let [msg (deserialize text)] 
    (i (format "<<< %s" msg))
    (reply msg)))

(defn start[] (s/consume listen c))

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
