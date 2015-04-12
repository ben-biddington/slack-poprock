(ns slack-poprock.human
  (:require [clojure.data.json :as json] [slack-poprock.message :as message]))

(defn- s[what] (if (nil? what) "" what))

(def ^{:private true} l (atom (fn[msg])))
(def ^{:private true} in-memory-slack (fn [channel text] (@l (format "Sending <%s> to channel <%s>" text channel))))
(def ^{:private true} slack-say (atom in-memory-slack))

(defn reply-with [slack] (reset! slack-say slack))
(defn log-with   [what]  (reset! l what) (println "Logging with: " @l))

(def ^{:private true} eating 
  [
   "http://media.tumblr.com/c7bce9315211947694acd3c032fcce0e/tumblr_inline_nmmff8U3Vl1ryolh9_500.gif"
   "http://c3.thejournal.ie/media/2014/04/bridesmaidsteeth.gif"
   "http://media2.giphy.com/media/ydkFnkSB53wqs/giphy.gif"])

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
(def ^{:private true} foods      ["chocolate" "licorice" "chipth" "icecream"])

(defn- mentioned-food?[msg] (message/mentioned-any? msg foods))
(defn- which-food?[msg] (first (filter #(message/mentioned? (:text msg) %) foods)))

(def actions [
 (fn[msg channel users]
   (when (mentioned-food? msg)
      #(@slack-say channel (str "Did someone say " (which-food? msg) "? " (rand-nth eating)))))

 (fn[msg channel users]
   (when
      (and 
        (message/dm? msg)
        (= (:text msg) "?"))
          (let [user-list (map #(:name %) users)]
            #(@slack-say channel (format "Thanks for asking, %s. Here are the people I know: %s" (message/from? msg users) (clojure.string/join ", " user-list))))))
 
 (fn[msg channel users]
   (when (or (message/mentioned-me? msg user-name nick-names) (message/dm? msg))
      #(@slack-say channel (rand-nth replies))))])

(defn- find-responder[msg channel users]
  (let [the-function (first (filter (fn[f] (not (nil? (apply f [msg channel users])))) actions))]
    (when-not (nil? the-function)
      (apply the-function [msg channel users]))))

(defn reply[to settings]
  (let [channel (:channel to) users (:users settings)]
    (let [r-fun (find-responder to channel users)]
      (when-not (nil? r-fun)
        (apply r-fun []))))) 
