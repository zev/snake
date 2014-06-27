(defproject snake "0.1.0-SNAPSHOT"
  :description "CLJS Snake game ported from Programming Clojure"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [domina "1.0.2"]]

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
