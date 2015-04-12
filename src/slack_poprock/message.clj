(ns slack-poprock.message)

(defn- s[what] (if (nil? what) "" what))
(defn- first-match[fn coll] (first (filter fn coll)))

(defn message?[msg]
  (let [type (:type msg)]
    (= "message" (s type))))

(defn mentioned?[text what]
   (.contains (.toLowerCase (s text)) (.toLowerCase what)))

(defn mentioned-any?[msg of-these] (some #(mentioned? (:text msg) %) of-these))

(defn dm?[msg]
  (let [channel (:channel msg)]
    (and 
     (message? msg) 
     (not (nil? channel)) 
     (= "D04B4FE3E" channel)))) ;; TODO: user id has to come out

(defn from?[msg users]
  (let [from (:user msg)]
    (:name (first-match #(= from (:id %)) users))))

(defn mentioned-me?[msg user-name nick-names]
  (or
   (mentioned? (:text msg) user-name) 
   (some #(mentioned? (:text msg) %) nick-names)))
