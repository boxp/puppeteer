(ns puppeteer.infra.repository.job
  (:require [com.stuartsierra.component :as component]
            [taoensso.faraday :as far]
            [puppeteer.infra.client.dynamodb :as dynamodbrepo]
            [puppeteer.domain.entity.job :refer [map->Job]]))

(defn get-job
  [{:keys [dynamodb-client] :as comp}
   id]
  (some-> (far/get-item (:opts dynamodb-client) :puppeteer-job {:id id})
          map->Job))

(defn set-job
  [{:keys [dynamodb-client] :as comp}
   {:keys [conf message build] :as job}]
  (far/update-item (:opts dynamodb-client) :puppeteer-job
                   {:id (-> job :build :id)}
                   {:conf [:put (far/freeze conf)]
                    :message [:put (far/freeze message)]
                    :build [:put (far/freeze build)]}))


(defrecord JobRepositoryComponent [dynamodb-client]
  component/Lifecycle
  (start [this]
    (println ";; Starting JobRepositoryComponent")
    this)
  (stop [this]
    (println ";; Stopping JobRepositoryComponent")
    this))

(defn job-repository-component
  []
  (map->JobRepositoryComponent {}))