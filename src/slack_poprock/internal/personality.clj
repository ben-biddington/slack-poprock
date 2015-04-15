(ns slack-poprock.internal.personality
  (:require 
   [clojure.string :as s :only [split-lines]] 
   [clojure.data.json :as json] 
   [clojure-watch.core :as watch] 
   [slack-poprock.message :as message]
   [slack-poprock.internal.log :as log :refer :all]))

(def ^{:private true} namespace-name (ns-name *ns*))
(def ^{:private true} config-file ".replies.conf")
(defn- load-replies[] (into [] (-> config-file slurp s/split-lines)))
(def ^{:private true} replies-config-file (atom (load-replies)))

(defn- start-watching []
  "Automaticaly reloads replies when file changes -- does not seem to work yet. @todo."
  (watch/start-watch [{
    :path config-file
    :event-types [:create :modify :delete]
    :bootstrap (#(log/info "Starting to watch <%s>" config-file))
    :callback (fn [event filename] (log/info "Reloading <%s>" filename))
    :options {:recursive true}}]))

(def ^{:private true} set-auto-load-on-once (memoize start-watching))

(defn auto-reload[] (apply set-auto-load-on-once []))

(def ^{:private true} last-few-replies (atom #{})) 

(defn- record[what] (swap! last-few-replies (fn[old] (take 5 (conj old what))) ))

(defn- ^{:private true} replies[]
  (let [all @replies-config-file last-few @last-few-replies]
    (let [pool (into '() (clojure.set/difference (into #{} all) last-few))]
      (log/info "Using a pool of %d/%d replies" (count pool) (count all))
      pool))) 

(defn retort[] 
  (let [reply (rand-nth (replies))]
    (do
      (record reply)
      reply)))

