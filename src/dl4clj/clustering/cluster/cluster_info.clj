(ns dl4clj.clustering.cluster.cluster-info
  (:import [org.deeplearning4j.clustering.info ClusterInfo]))

;; not sure the workflow of how cluster info gets the info from the cluster
;; this might happen behind the scenes but I think this is something a user
;; has to set up

;; should be combined with cluster-set-info
;; you can only add info to clusters via that class

(defn new-cluster-info-obj
  [& {:keys [thread-safe?]
      :as opts}]
  (if (contains? opts :tread-safe?)
    (ClusterInfo. thread-safe? false)
    (ClusterInfo. false)))

(defn get-avg-point-distance-from-center
  [cluster-info]
  (.getAveragePointDistanceFromCenter cluster-info))

(defn get-max-point-distance-from-center
  [cluster-info]
  (.getMaxPointDistanceFromCenter cluster-info))

(defn get-point-distance-from-center-variance
  [cluster-info]
  (.getPointDistanceFromCenterVariance cluster-info))

(defn get-point-distance-from-center
  [cluster-info]
  (.getPointDistancesFromCenter cluster-info))

(defn get-points-farther-from-center-than
  [& {:keys [cluster-info max-distance]}]
  (.getPointsFartherFromCenterThan cluster-info max-distance))

(defn get-reverse-sorted-point-distances-from-center
  [cluster-info]
  (.getReverseSortedPointDistancesFromCenter cluster-info))

(defn get-sorted-point-distances-from-center
  [cluster-info]
  (.getSortedPointDistancesFromCenter cluster-info))

(defn get-total-point-distance-from-center
  [cluster-info]
  (.getTotalPointDistanceFromCenter cluster-info))

(defn set-avg-point-distance-from-center!
  [& {:keys [cluster-info avg-distance]}]
  (doto cluster-info (.setAveragePointDistanceFromCenter avg-distance)))

(defn set-max-point-distance-from-center!
  [& {:keys [cluster-info max-distance]}]
  (doto cluster-info (.setMaxPointDistanceFromCenter max-distance)))

(defn set-point-distance-from-center-variance!
  [& {:keys [cluster-info distance-variance]}]
  (doto cluster-info (.setPointDistanceFromCenterVariance distance-variance)))

(defn set-point-distances-from-center!
  [& {:keys [cluster-info distances]}]
  (doto cluster-info (.setPointDistancesFromCenter distances)))

(defn set-total-point-distance-from-center!
  [& {:keys [cluster-info sum-squared-error]}]
  (doto cluster-info (.setTotalPointDistanceFromCenter sum-squared-error)))
