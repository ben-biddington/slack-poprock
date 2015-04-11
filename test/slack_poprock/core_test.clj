(ns slack-poprock.core-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [cheshire.core :refer :all :as c]
            [slack-poprock.slack :refer :all :as slack]))

(defn- json-pretty[what] (c/generate-string what {:pretty true}))

(def ^{:private true} token (System/getenv "TOKEN")) ;; => https://trapslinger.slack.com/services/4345477538?icon=1

(deftest that-you-can-query-slack-for-various-data
  (testing "List users like this, for example"
    (let [users (slack/users token)]
      (is (some #(= "beans" (:name %)) users) "Expected to find a user called \"beans\""))))

;; TEST: can ask who a message is from
