(defproject snake "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.async "0.1.256.0-1bf8cf-alpha"]
                 [domina "1.0.2"]
                 ]

  :plugins [[lein-cljsbuild "1.0.1"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "snake"
              :source-paths ["src"]
              :compiler {
                :output-to "snake.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
