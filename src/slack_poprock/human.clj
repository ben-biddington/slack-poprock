(ns slack-poprock.human
  (:require 
   [clojure.string :as s :only [split-lines]] 
   [clojure.data.json :as json] 
   [clojure-watch.core :as watch]
   [slack-poprock.internal.personality :refer :all :as p]
   [slack-poprock.message :as message]
   [slack-poprock.internal.log :as log :refer :all]))

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

(def ^{:private true} user-name  "@U1E3JRH")
(def ^{:private true} nick-names ["poprock", "poppo" "rick" "p-rick" "ricky" "mark wigg" "rocky"])
(def ^{:private true} foods      ["chocolate" "licorice" "chipth" "icecream" "chops" "steak"])

(defn- mentioned-food?[msg] (message/mentioned-any? msg foods))
(defn- which-food?[msg] (first (filter #(message/mentioned? (:text msg) %) foods)))
(defn- names[from] (map #(:name %) from))

(def actions [
 (fn[msg channel settings]
   (when (mentioned-food? msg)
      #(@slack-say channel (str "Did someone say " (which-food? msg) "?"))))

 (fn[msg channel settings]
   (when
      (and 
        (message/dm? msg)
        (= (:text msg) "?"))
          (let [user-list (names (:users settings)) channel-list (names (:channels settings))]
            #(@slack-say channel 
              (format 
               "Thanks for asking, %s. Here are the people I know: %s. And here are all of the channels: %s" 
               (message/from? msg (:users settings)) 
               (clojure.string/join ", " user-list)
               (clojure.string/join ", " channel-list))))))
 
 (fn[msg channel settings]
   (when (or (message/mentioned-me? msg user-name nick-names) (message/dm? msg))
      #(@slack-say channel (p/retort))))])

(defn- find-responder[msg channel settings]
  (let [the-function (first (filter (fn[f] (not (nil? (apply f [msg channel settings])))) actions))]
    (when-not (nil? the-function)
      (apply the-function [msg channel settings]))))

(defn reply[to settings]
  (p/auto-reload)
  (let [channel (:channel to)]
    (let [r-fun (find-responder to channel settings)]
      (when-not (nil? r-fun)
        (apply r-fun []))))) 
