(defproject simix "0.2.1"
  :description "Similarity index for Clojure"
  :url "https://github.com/tsers/simix"
  :deploy-repositories [["releases" :clojars]]
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}
  :plugins [[lein-ancient "0.6.15"]]
  :auto-clean false
  :dependencies [[net.java.dev.jna/jna "5.0.0"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :resource-paths ["resources"]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.9.0"]
                                  [commons-io "2.6"]]}
             :jar {:dependencies [[org.clojure/clojure "1.9.0"]]}}
  :aliases {"t" ["test"]}
  :release-tasks [["deploy"]])
