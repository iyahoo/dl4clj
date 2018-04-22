(ns dl4clj.clustering.cluster.cluster-set-info
  (:import [org.deeplearning4j.clustering.info ClusterSetInfo]))

;; same considerations from cluster-info apply here

(defn new-cluster-set-info-obj
  [& {:keys [thread-safe?]
      :as opts}]
  (if (contains? opts :thread-safe?)
    (ClusterSetInfo. false thread-safe?)
    (ClusterSetInfo. false)))

(defn add-cluster-info
  [& {:keys [info cluster-id]}]
  (.addClusterInfo info cluster-id))

(defn initialize
  ;; looks like the main fn to call
  [& {:keys [cluster-set thread-safe?]}]
  (ClusterSetInfo/initialize cluster-set thread-safe?))

(defn get-cluster-info
  [& {:keys [cluster-set-info cluster-id]}]
  (.getClusterInfo cluster-set-info cluster-id))

(defn get-clusters-info
  [cluster-set-info]
  (.getClustersInfos cluster-set-info))

(defn get-avg-point-distance-from-cluster-center
  [cluster-set-info]
  (.getAveragePointDistanceFromClusterCenter cluster-set-info))

(defn get-distance-between-cluster-centers
  [cluster-set-info]
  (.getDistancesBetweenClustersCenters cluster-set-info))

(defn get-point-distance-from-cluster-variance
  [cluster-set-info]
  (.getPointDistanceFromClusterVariance cluster-set-info))

(defn get-point-location-change
  [cluster-set-info]
  (.getPointLocationChange cluster-set-info))

(defn get-n-points
  [cluster-set-info]
  (.getPointsCount cluster-set-info))

(defn remove-cluster-infos!
  [& {:keys [cluster-set-info clusters]}]
  ;; check which obj should be in the doto with testing
  (doto cluster-set-info (.removeClusterInfos clusters)))

(defn set-clusters-info!
  [& {:keys [cluster-set-info clusters-infos]}]
  (doto cluster-set-info (.setClustersInfos clusters-infos)))

(defn set-distance-between-clusters-centers!
  [& {:keys [cluster-set-info inter-cluster-distances]}]
  (doto cluster-set-info (.setDistancesBetweenClustersCenters cluster-set-info
                                                              inter-cluster-distances)))

(defn set-point-location-change!
  [& {:keys [cluster-set-info point-location-change]}]
  (doto cluster-set-info (.setPointLocationChange point-location-change)))
