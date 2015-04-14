(ns slack-poprock.slack
  (:require [clojure.data.json :as json]
            [manifold.stream :as s]
            [aleph.http :as http]
            [clj-http.client :as client]))

(defrecord User    [name real-name id title])
(defrecord Channel [name id creator-id])

(defn- clean[text] (if (clojure.string/blank? text) "" text))

(defn- to-user [what] 
  (User. 
   (-> what :name clean) 
   (-> what :real_name clean) 
   (-> what :id) 
   (-> what :profile :title clean)))

(defn- to-channel [what] 
  (Channel. 
   (-> what :name clean) 
   (-> what :id)
   (-> what :creator)))

(defn- to-users    [what] (map to-user what))
(defn- to-channels [what] (map to-channel what))

(defn- deserialize      [what]  (json/read-str what :key-fn keyword))

(defn- start-url        [token] (str "https://slack.com/api/rtm.start?token=" token))
(defn- slack-start-info [token] (deserialize (:body (client/get (start-url token)))))
(defn- slack-start-url  [token] (:url (slack-start-info token)))

(defn- settings          [token] (slack-start-info token))
(defn users             [token] (to-users (:users (settings token))))
(defn channels          [token] (to-channels (:channels (settings token))))
