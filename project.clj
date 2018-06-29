(defproject forest "0.1.0-SNAPSHOT"
  :description "Simulate the growth of a forest"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.8.0"]
                        [penumbra "0.6.1"]]
  :main ^:skip-aot forest.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
