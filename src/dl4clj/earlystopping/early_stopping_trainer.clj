(ns ^{:doc "ns for creating early stopping trainers"}
    dl4clj.earlystopping.early-stopping-trainer
  (:import [org.deeplearning4j.earlystopping.trainer
            EarlyStoppingTrainer BaseEarlyStoppingTrainer]
           [org.deeplearning4j.spark.earlystopping SparkEarlyStoppingTrainer])
  (:require [dl4clj.utils :refer [contains-many? obj-or-code? eval-if-code]]
            [clojure.core.match :refer [match]]
            [dl4clj.helpers :refer [reset-iterator!]]))

(defn new-early-stopping-trainer
  "onducting early stopping training locally (single machine), for training a MultiLayerNetwork.

  :early-stopping-conf (early-stopping-configuration),
   - see: dl4clj.earlystopping.early-stopping-config

  :mln (model or multi-layer-conf), a built multilayer network or the configuration for a multilayer network
   - see: dl4clj.nn.conf.builders.multi-layer-builders and dl4clj.nn.multilayer.multi-layer-network

  :iter (dataset-iterator), an iterator for a dataset
   - see: dl4clj.datasets.iterators

  see: https://deeplearning4j.org/doc/org/deeplearning4j/earlystopping/trainer/EarlyStoppingTrainer.html"
  [& {:keys [early-stopping-conf mln iter as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:early-stopping-conf (_ :guard seq?)
           :mln (_ :guard seq?)
           :iter (_ :guard seq?)}]
         (obj-or-code? as-code? `(EarlyStoppingTrainer. ~early-stopping-conf ~mln ~iter))
         [{:early-stopping-conf _
           :mln _
           :iter _}]
         (let [[es-conf-obj mln-obj iter-obj] (eval-if-code [early-stopping-conf seq?]
                                                            [mln seq?]
                                                            [iter seq?])]
           (EarlyStoppingTrainer. es-conf-obj mln-obj iter-obj))))

(defn new-spark-early-stopping-trainer
  ;; TODO this needs to be addapted to work with the no multiple spark context issues
  "object for conducting early stopping training via Spark with
   multi-layer networks

  :spark-context (spark), Can be either a JavaSparkContext or a SparkContext
   - need to make a ns dedicated to making JavaSparkContexts

  :training-master (training-master), the object which sets options for training on spark
   - see: dl4clj.spark.masters.param-avg

  :early-stopping-conf (config), configuration for early stopping
   - see: dl4clj.earlystopping.early-stopping-config

  :mln (MultiLayerNetwork), the neural net to be trained
   - see: dl4clj.nn.multilayer.multi-layer-network for creating one from a nn-conf

  :training-rdd (JavaRdd <DataSet>), a dataset contained within a Java RDD.
   - the data for training

  :early-stopping-listener (listener), a listener which implements the EarlyStoppingListener interface
   - see: dl4clj.earlystopping.interfaces.listener
   - NOTE: need to figure out if this requires a gen class or not
   - optional arg

  see: https://deeplearning4j.org/doc/org/deeplearning4j/spark/earlystopping/SparkEarlyStoppingTrainer.html"
  [& {:keys [spark-context training-master early-stopping-conf
             mln training-rdd early-stopping-listener as-code?]
      :or {as-code? true}
      :as opts}]
  (match [opts]
         [{:spark-context (_ :guard seq?)
           :training-master (_ :guard seq?)
           :early-stopping-conf (_ :guard seq?)
           :mln (_ :guard seq?)
           :training-rdd (_ :guard seq?)
           :early-stopping-listener (_ :guard seq?)}]
         (obj-or-code?
          as-code?
          `(SparkEarlyStoppingTrainer.
           ~spark-context ~training-master ~early-stopping-conf
           ~mln ~training-rdd ~early-stopping-listener))
         [{:spark-context _
           :training-master _
           :early-stopping-conf _
           :mln _
           :training-rdd _
           :early-stopping-listener _}]
         (let [[sc-obj master-obj conf-obj mln-obj rdd-obj es-listen-obj]
               (eval-if-code [spark-context seq?] [training-master seq?]
                             [early-stopping-conf seq?] [mln seq?]
                             [training-rdd seq?] [early-stopping-listener seq?])]
           (SparkEarlyStoppingTrainer. sc-obj master-obj conf-obj mln-obj
                                       rdd-obj es-listen-obj))
         [{:spark-context (_ :guard seq?)
           :training-master (_ :guard seq?)
           :early-stopping-conf (_ :guard seq?)
           :mln (_ :guard seq?)
           :training-rdd (_ :guard seq?)}]
         (obj-or-code?
          as-code?
          `(SparkEarlyStoppingTrainer.
           ~spark-context ~training-master ~early-stopping-conf
           ~mln ~training-rdd))
         [{:spark-context _
           :training-master _
           :early-stopping-conf _
           :mln _
           :training-rdd _}]
         (let [[sc-obj master-obj conf-obj mln-obj rdd-obj]
               (eval-if-code [spark-context seq?] [training-master seq?]
                             [early-stopping-conf seq?] [mln seq?]
                             [training-rdd seq?])]
           (SparkEarlyStoppingTrainer. sc-obj master-obj conf-obj mln-obj rdd-obj))))
