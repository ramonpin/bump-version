(defproject bump-version "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-jgit "0.8.10"]
                 [environ "1.1.0"]]
  :main ^:skip-aot bump-version.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

