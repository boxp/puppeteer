(defproject sorcerer "0.1.0-SNAPSHOT"
  :description "A Slackbot for Deploying microservices to GKE Cluster"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [environ "1.1.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [org.julienxx/clj-slack "0.5.5"]
                 [slack-rtm "0.1.5"]
                 [cheshire "5.7.1"]
                 [circleci/clj-yaml "0.5.5"]
                 [tentacles "0.5.1"]
                 [org.clojure/tools.namespace "0.2.10"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.11.95"]
                 [com.taoensso/faraday "1.8.0"
                  :exclusions [[com.amazonaws/aws-java-sdk-dynamodb]]]
                 [io.fabric8/kubernetes-client "2.5.6"]
                 [io.fabric8/kubernetes-model "1.1.0"]
                 [com.google.guava/guava "22.0"]
                 [com.google.auth/google-auth-library-oauth2-http "0.6.1"]
                 [com.google.auth/google-auth-library-credentials "0.6.1"]
                 [com.google.cloud/google-cloud-pubsub "0.20.1-beta"
                  :exclusions [com.google.auth/google-auth-library-oauth2-http
                               com.google.auth/google-auth-library-credentials
                               com.google.guava/guava]]
                 [com.google.apis/google-api-services-cloudbuild "v1-rev20210511-1.31.0"
                  :exclusions [com.google.guava/guava
                               com.google.guava/guava-jdk5]]
                 [com.google.apis/google-api-services-dns "v1-rev44-1.22.0"]]
  :profiles
  {:dev {:source-paths ["src" "dev"]}
   :uberjar {:main sorcerer.system}})
