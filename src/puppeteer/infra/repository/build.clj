(ns puppeteer.infra.repository.build
  (:import (com.google.api.services.cloudbuild.v1.model Build Source RepoSource BuildStep Secret))
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [go put! <! close! chan]]
            [cheshire.core :refer [parse-string]]
            [puppeteer.domain.entity.build :as entity]
            [puppeteer.infra.client.container-builder :as gccb-cli]
            [puppeteer.infra.client.pubsub :as pubsub-cli]))

(def default-build-timeout
  "600.0s")

(defn- build->Build
  [build]
  (let [repo-source (doto (RepoSource.)
                      (.setProjectId (-> build :source :repo-source :project-id))
                      (.setRepoName (-> build :source :repo-source :repo-name))
                      (.setBranchName (-> build :source :repo-source :branch-name))
                      (.setTagName (-> build :source :repo-source :tag-name))
                      (.setCommitSha (-> build :source :repo-source :commit-sha)))
        source (doto (Source.)
                 (.setRepoSource repo-source))
        steps (map #(doto (BuildStep.)
                      (.setArgs (-> % :args))
                      (.setName (-> % :name))
                      (.setEnv (or (:env %) []))
                      (.setSecretEnv (or (:secretEnv %) [])))
                   (-> build :steps))
        images (-> build :images)
        timeout (or (:timeout build) default-build-timeout)
        secrets (map #(doto (Secret.)
                        (.setKmsKeyName (:kmsKeyName %))
                        (.setSecretEnv (java.util.HashMap. (:secretEnv %))))
                     (or (:secrets build) []))]
    (doto (Build.)
      (.setSource source)
      (.setSteps steps)
      (.setImages images)
      (.setTimeout timeout)
      (.setSecrets secrets))))

(defn- BuildMessage->build-message
  [m]
  (some-> m
          .getData
          .toStringUtf8
          (parse-string true)
          entity/map->BuildMessage))

(defn- Operation->BuildId
  [operation]
  (some-> operation
          .getMetadata
          (.get "build")
          (.get "id")))

(defn create-build
  [{:keys [container-builder-client] :as comp} build]
  (->> build
       build->Build
       (gccb-cli/create-build container-builder-client)
       Operation->BuildId))

(def topic-key :cloud-builds)

(defn get-build-message
  [{:keys [container-builder-client] :as comp}]
  (:channel comp))

(defrecord BuildRepositoryComponent [container-builder-client pubsub-subscription subscription-key]
  component/Lifecycle
  (start [this]
    (let [c (chan)]
      (println ";; Starting BuildRepositoryComponent")
      (try
        (pubsub-cli/create-subscription (:pubsub-subscription this) topic-key subscription-key)
        (catch Exception e (println "Warning: Already" subscription-key "has exists")))
      (-> this
          (update :pubsub-subscription
                  #(pubsub-cli/add-subscriber % topic-key subscription-key
                                              (fn [m]
                                                (put! c (BuildMessage->build-message m)))))
          (assoc :channel c))))
  (stop [this]
    (println ";; Stopping BuildRepositoryComponent")
    (doall (map #(.stopAsync %) (-> this :pubsub-subscription :subscribers)))
    (close! (:channel this))
    (-> this
        (dissoc :channel))))

(defn build-repository-component
  [subscription-name]
  (map->BuildRepositoryComponent {:subscription-key (keyword subscription-name)}))
