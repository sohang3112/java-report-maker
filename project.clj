(defproject java-report-maker "0.1.0-SNAPSHOT"
  :description "Run all Java programs in a folder, and make a report containing source code and output"
  :url "https://github.com/sohang3112/java-report-maker"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.apache.commons/commons-lang3 "3.5"]
                 [commons-io/commons-io "2.11.0"]
                 [me.raynes/fs "1.4.6"]
                 [net.openhft/compiler "2.4.1" :type "pom"]]
  :main ^:skip-aot java-report-maker.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
