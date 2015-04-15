(ns slack-poprock.internal.personality
  (:require 
   [clojure.string :as s :only [split-lines]] 
   [clojure.data.json :as json] 
   [clojure-watch.core :as watch] 
   [slack-poprock.message :as message]))

(def ^{:private true} l (atom (fn[msg])))
(def ^{:private true} namespace-name (ns-name *ns*))
(def ^{:private true} config-file "/home/ben/sauce/slack-poprock/.replies.conf")
(defn- load-replies[] (-> config-file slurp s/split-lines))
(def ^{:private true} replies-config-file (atom (load-replies)))

(defn- start-watching []
  "Automaticaly reloads replies when file changes"
  (watch/start-watch [{
    :path config-file
    :event-types [:create :modify :delete]
    :bootstrap (#(@l (format "[%s] Starting to watch <%s>" namespace-name  config-file)))
    :callback (fn [event filename] (@l (format "[%s] Reloading <%s>" namespace-name filename)))
    :options {:recursive true}}]))

(def ^{:private true} set-auto-load-on-once (memoize start-watching))

(defn auto-reload[] (apply set-auto-load-on-once []))

(def replies @replies-config-file)

