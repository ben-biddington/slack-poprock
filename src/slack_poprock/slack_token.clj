(ns slack-poprock.slack-token)

(def current      
  (or 
   (System/getenv "TOKEN") 
   (slurp ".token"))) ;; => https://trapslinger.slack.com/services/4345477538?icon=1
