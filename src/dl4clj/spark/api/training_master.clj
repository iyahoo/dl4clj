(ns ^{:doc "A TrainingMaster controls how distributed training is executed in practice

In principle, a large number of different approches can be used in distributed training (synchronous vs. asynchronous, parameter vs. gradient averaging, etc).

 Each of these different approaches would be implemented as a TrainingMaster; this allows SparkDl4jMultiLayer and SparkComputationGraph to be used with different training methods.

see: https://deeplearning4j.org/doc/org/deeplearning4j/spark/api/TrainingMaster.html"}
    dl4clj.spark.api.training-master
  (:import [org.deeplearning4j.spark.api TrainingMaster])
  (:require [clojure.core.match :refer [match]]
            [dl4clj.utils :refer [obj-or-code?]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; getters
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-training-stats
  "returns the training stats"
  [& {:keys [master as-code?]
      :or {as-code? true}}]
  (match [master]
         [(_ :guard seq?)]
         (obj-or-code? as-code? `(.getTrainingStats ~master))
         :else
         (.getTrainingStats master)))

(defn get-worker
  "returns the work instance for this training mater"
  [& {:keys [master spark-mln as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:master (_ :guard seq?)
           :spark-mln (_ :guard seq?)}]
         (obj-or-code? as-code? `(.getWorkerInstance ~master ~spark-mln))
         :else
         (.getWorkerInstance master spark-mln)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; setters
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn set-collection-training-stats?
  "Set whether the training statistics should be collected.
   - defaults to true

  returns the master"
  [& {:keys [master collect-stats? as-code?]
      :or {collect-stats? true
           as-code? true}
      :as opts}]
  (match [opts]
         [{:master (_ :guard seq?)
           :collect-stats? (:or (_ :guard boolean?)
                                (_ :guard seq?))}]
         (obj-or-code? as-code? `(doto ~master (.setCollectTrainingStats ~collect-stats?)))
         [{:master _
           :collect-stats? _}]
         (doto master (.setCollectTrainingStats collect-stats?))
         [{:master (_ :guard seq?)}]
         (obj-or-code? as-code? `(doto ~master (.setCollectTrainingStats ~collect-stats?)))
         :else
         (doto master (.setCollectTrainingStats collect-stats?))))

(defn set-master-listeners!
  "Set the iteration listeners and the StatsStorageRouter for master.

  :listeners (coll), a collection of listeners
   - see: dl4clj.optimize.listeners.listeners

  :stats-storage-router (storage) (optional)
   - not yet implemented, will be in dl4clj.api.storage
   - for now, see: https://deeplearning4j.org/doc/org/deeplearning4j/api/storage/StatsStorageRouter.html

  returns master"
  [& {:keys [master stats-storage-router listeners as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:master (_ :guard seq?)
           :stats-storage-router (_ :guard seq?)
           :listeners (:or (_ :guard coll?)
                           (_ :guard seq?))}]
         (obj-or-code?
          as-code?
          `(doto ~master (.setListeners ~stats-storage-router ~listeners)))
         [{:master _
           :stats-storage-router _
           :listeners _}]
         (doto master (.setListeners stats-storage-router listeners))
         [{:master (_ :guard seq?)
           :listeners (:or (_ :guard coll?)
                           (_ :guard seq?))}]
         (obj-or-code? as-code? `(doto ~master (.setListeners ~listeners)))
         [{:master _
           :listeners _}]
         (doto master (.setListeners listeners))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; misc
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; come back to these and determine best return values
;; param-avg-master currently only implementer

(defn delete-temp-files
  "Attempt to delete any temporary files generated by this TrainingMaster.

  :spark-context can be of type:
   1) org.apache.spark.api.java.JavaSparkContext
   2) org.apache.spark.SparkContext

  returns a boolean indicating if the delete was successful"
  [& {:keys [master spark-context as-code?]
      :or {as-code? true}
      :as opts}]
  ;; currently gives no indication of success
  (match [opts]
         [{:master (_ :guard seq?)
           :spark-context (_ :guard seq?)}]
         (obj-or-code? as-code? `(doto ~master (.deleteTempFiles ~spark-context)))
         :else
         (doto master (.deleteTempFiles spark-context))))

(defn execute-training!
  "Train the SparkDl4jMultiLayer with the specified data set

  :spark-mln (SparkDl4jmultilayer), the spark representation of a mln
   - implementation not yet created,
   - see: https://deeplearning4j.org/doc/org/deeplearning4j/spark/impl/multilayer/SparkDl4jMultiLayer.html

  :rdd (javaRDD), the dataset to train on
   - a dataset wrapped in an org.apache.spark.api.java.JavaRDD obj"
  [& {:keys [spark-mln master rdd as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:spark-mln (_ :guard seq?)
           :master (_ :guard seq?)
           :rdd (_ :guard seq?)}]
         (throw (Exception. "spark mlns and rdds must be objects"))
         :else
         (doto master (.executeTraining spark-mln rdd))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn collecting-training-stats?
  "checks to see if spark training stats are being collected"
  [& {:keys [master as-code?]
      :or {as-code? true}}]
  (match [master]
         [(_ :guard seq?)]
         (obj-or-code? as-code? `(.getIsCollectTrainingStats ~master))
         :else
         (.getIsCollectTrainingStats master)))
