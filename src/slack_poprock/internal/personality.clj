(ns slack-poprock.internal.personality
  (:refer-clojure :except [time])
  (:require 
   [clojure.string :as s :only [split-lines]] 
   [clojure.data.json :as json] 
   [clojure-watch.core :as watch]
   [clj-time.core :as t] 
   [slack-poprock.message :as message]
   [slack-poprock.internal.log :as log :refer :all]
   [slack-poprock.internal.diagnostics :as diag :refer :all]))

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

(defn- record[what] (swap! last-few-replies (fn[old] (take 10 (conj old what))) ))

(defn- ^{:private true} replies[]
  (let [all @replies-config-file last-few @last-few-replies]
    (let [pool (into '() (clojure.set/difference (into #{} all) last-few))]
      (log/info "Using a pool of %d/%d replies (ignored these recent ones: <%s>)" (count pool) (count all) (clojure.string/join "," last-few))
      pool))) 

(defn retort[] 
  (let [reply (diag/time #(rand-nth (replies)))]
    (do
      (record (:result reply))
      (log/info "It took %sms to decide on a reply" (t/in-millis (:duration reply)))
      (:result reply))))

