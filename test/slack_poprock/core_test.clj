(ns slack-poprock.core-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [cheshire.core :refer :all :as c]
            [slack-poprock.slack :refer :all :as slack]
            [slack-poprock.slack-token :refer :all :as t]))

(defn- json-pretty[what] (c/generate-string what {:pretty true}))

(def ^{:private true} token t/current) 

(deftest that-you-can-query-slack-for-various-data
  (testing "List users like this, for example"
    (let [users (slack/users token)]
      (is (some #(= "beans" (:name %)) users) "Expected to find a user called \"beans\"")))
  (testing "List channels"
    (let [channels (slack/channels token)]
      (is (some #(= "random" (:name %)) channels) "Expected to find a channel called \"random\""))))

;; TEST: can ask who a message is from
