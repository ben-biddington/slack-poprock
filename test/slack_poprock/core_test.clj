(ns slack-poprock.core-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [cheshire.core :refer :all :as c]
            [slack-poprock.slack :refer :all :as slack]))

(def ^{:private true} token (System/getenv "TOKEN")) ;; => https://trapslinger.slack.com/services/4345477538?icon=1

(deftest that-you-can-query-slack-for-various-data
  (testing "List contacts like this, for example"
    (let [settings (slack/settings token)]
      (println (c/generate-string settings {:pretty true})))))
