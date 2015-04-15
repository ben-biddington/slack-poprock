(defproject slack-poprock "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [aleph "0.4.0-beta3"]
                 [clj-http "1.1.0"]
                 [org.clojure/data.json "0.2.6"]
                 [cheshire "5.4.0"]
                 [clojure-watch "LATEST"]]
  :main ^:skip-aot slack-poprock.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
