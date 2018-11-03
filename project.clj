(defproject simix "0.0.1"
  :description "Similarity index for Clojure"
  :url "https://github.com/tsers/simix"
  :deploy-repositories [["releases" :clojars]]
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}
  :plugins [[lein-ancient "0.6.15"]]
  :dependencies []
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :resource-paths ["resources"]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.9.0"]]}}
  :aliases {}
  :release-tasks [["deploy"]])
