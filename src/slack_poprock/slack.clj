(ns slack-poprock.slack
  (:require [clojure.data.json :as json]
            [manifold.stream :as s]
            [aleph.http :as http]
            [clj-http.client :as client]))

(defn- deserialize      [what]  (json/read-str what :key-fn keyword))
(defn- start-url        [token] (str "https://slack.com/api/rtm.start?token=" token))
(defn- slack-start-info [token] (deserialize (:body (client/get (start-url token)))))
(defn- slack-start-url  [token] (:url (slack-start-info token)))

(defn settings          [token] (slack-start-info token))
;;(defn connect-to[url] (aleph.http/websocket-client url))

