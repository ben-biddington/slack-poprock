(ns slack-poprock.human
  (:require [clojure.data.json :as json]))

(def ^{:private true} l (atom (fn[msg])))

(def ^{:private true} in-memory-slack (fn [channel text] (@l (format "Sending <%s> to channel <%s>" text channel))))

(def ^{:private true} slack-say (atom in-memory-slack))

(defn reply-with [slack] (reset! slack-say slack))
(defn log-with   [what]  (reset! l what) (println "Logging with: " @l))

(def ^{:private true} replies
  [
    "Heck Mandy!"
    "Ma balls!"
    "Timm-o!"
    "You bet, bubba!"
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
    "Joss-ette!"
    "Who writes your jokes?"])

(def ^{:private true} user-name  "@U04B4FE2Y")
(def ^{:private true} nick-names ["poppo" "rick" "p-rick" "ricky" "mark wigg"])
(def ^{:private true} foods      ["chocolate" "licorice" "chipth"])
(defn- s[what] (if (nil? what) "" what))
(defn- message?[msg]
  (let [type (:type msg)]
    (= "message" (s type))))

(defn- mentioned?[text what]
  (and 
   (not (clojure.string/blank? text)) 
   (.contains (.toLowerCase text) (.toLowerCase what))))

(defn- mentioned-me?[msg]
  (or
   (mentioned? (:text msg) user-name) 
   (some #(mentioned? (:text msg) %) nick-names)))

(defn- mentioned-food?[msg] (some #(mentioned? (:text msg) %) foods))

(defn- which-food?[msg] (first (filter #(mentioned? (:text msg) %) foods)))
(defn- first-match[fn coll] (first (filter fn coll)))

(defn- from?[msg users]
  (let [from (:user msg)]
    (:name (first-match #(= from (:id %)) users))))

(defn- dm?[msg]
  (let [channel (:channel msg)]
    (and 
     (message? msg) 
     (not (nil? channel)) 
     (= "D04B4FE3E" channel))))

(defn reply[to settings]
  (let [channel (:channel to) users (:users settings)]
    (when
      (and 
        (dm? to)
        (= (:text to) "?"))
          (let [user-list (map #(:name %) users)]
            (@slack-say channel (format "Thanks for asking, %s. Here are the people I know: %s" (from? to users) (clojure.string/join ", " user-list)))))

    (when (or (mentioned-me? to) (dm? to))
      (@slack-say channel (rand-nth replies)))

    (when (mentioned-food? to)
      (@slack-say channel (str "Did someone say " (which-food? to) "?")))))
