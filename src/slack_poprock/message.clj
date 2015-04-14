(ns slack-poprock.message)

(defn- s[what] (if (nil? what) "" what))
(defn- first-match[fn coll] (first (filter fn coll)))

(defn message?[msg]
  (let [type (:type msg)]
    (= "message" (s type))))

(defn mentioned?[text what]
   (.contains (.toLowerCase (s text)) (.toLowerCase what)))

(defn mentioned-any?[msg of-these] (some #(mentioned? (:text msg) %) of-these))

(defn dm?
  " A channel that starts with 'D' is direct message. Public channels start with 'C'. First one below is DM:
    [Tue Apr 14 13:14:23 NZST 2015] <<< {:type 'message', :channel 'D04B4FE3E', :user 'U04AUKBGT', :text '?'}
    [Tue Apr 14 13:20:22 NZST 2015] <<< {:type 'user_typing', :channel 'C04A73LMW', :user 'U04AUKBGT'}"
  [msg]
  (let [channel (:channel msg)]
    (and 
     (message? msg) 
     (not (nil? channel)) 
     (.startsWith channel "D"))))

(defn from?[msg users]
  (let [from (:user msg)]
    (:name (first-match #(= from (:id %)) users))))

(defn mentioned-me?[msg user-name nick-names]
  (or
   (mentioned? (:text msg) user-name) 
   (some #(mentioned? (:text msg) %) nick-names)))
