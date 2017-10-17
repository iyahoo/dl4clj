(ns dl4clj.dev.usr
  (:require [dl4clj.datasets.input-splits :as s]
            [dl4clj.datasets.record-readers :as rr]
            [dl4clj.datasets.api.record-readers :refer :all]
            [dl4clj.datasets.iterators :as ds-iter]
            [dl4clj.datasets.api.iterators :refer :all]
            [dl4clj.helpers :refer [data-from-iter reset-if-empty?!]]
            [nd4clj.linalg.factory.nd4j :refer [vec->indarray matrix->indarray
                                                indarray-of-zeros indarray-of-ones
                                                indarray-of-rand vec-or-matrix->indarray]]
            [dl4clj.datasets.new-datasets :refer [new-ds]]
            [dl4clj.datasets.api.datasets :refer [as-list]]
            [dl4clj.datasets.iterators :refer [new-existing-dataset-iterator]])
  #_(:require [dl4clj.nn.conf.builders.multi-layer-builders :as mlb]
            [dl4clj.nn.conf.builders.nn-conf-builder :as nn-conf]
            [dl4clj.nn.conf.builders.builders :as l]
            ;;[clj-time.core :as t]
            ;;[clj-time.format :as tf]
            [datavec.api.split :as f]
            [datavec.api.records.readers :as rr]
            [dl4clj.datasets.datavec :as ds]
            [nd4clj.linalg.dataset.api.data-set :as d]
            [dl4clj.eval.evaluation :as e]
            [dl4clj.nn.multilayer.multi-layer-network :as mln])
  (:import [org.deeplearning4j.optimize.listeners ScoreIterationListener]
           [org.datavec.api.transform.schema Schema Schema$Builder]
           [org.datavec.api.transform TransformProcess$Builder TransformProcess]
           [org.apache.spark SparkConf]
           [org.apache.spark.api.java JavaRDD JavaSparkContext]
           [org.datavec.spark.transform SparkTransformExecutor]
           [org.datavec.spark.transform.misc StringToWritablesFunction]
           [java.util.List]
           ))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; loading data to be used in a model
;; the flow is:
;; 1) create an input split
;; 2) create a record reader
;; 3) initialize the record reader using the input split
;; 4) create an iterator from the initialized record reader
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 1) create an input split
;; how to pick an input split:
;; what is the source of my data?
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;




;; - set up flow from input split + rr (you must initialize a rr with a is) -> iter




;; doc string should refrence
;; dl4clj.datasets.input-splits
;; - for the creation of input splits
;; dl4clj.datasets.record-readers
;; - for the creation of record readers
;; these are needed to call dl4clj.datasets.api.record-readers/initialize-rr!
;; once rr is initialized, we can create our iterators
;; reference dl4clj.datasets.iterators

;; could always move user facing fns here




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; create model
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; creation of nn-config stays where it is
;; here we want create the multi layer network and initialize it

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; train model on data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; - this is where train-model! comes in

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; evaluate model
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; - dl4clj.nn.api.multi-layer-network/evaluate-classification -> create eval obj
;; then get the stats from the object

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; refine model
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ability to create an early stopping trainer from the mln







#_(defn train-model!
  ;; need to indicate that this also works with early stopping trainers
  "Fit/train the multi layer network

  :mln

  if you supply an iterator, it is only reset if it is at the end of the collection"
  ;; data is for unsupervised learning
  [& {:keys [mln dataset iter data labels features features-mask
             labels-mask examples label-idxs]
      :as opts}]
  (match [opts]
         [{:mln (_ :guard seq?)
           :features (:or (_ :guard vector?)
                          (_ :guard seq?))
           :labels (:or (_ :guard vector?)
                        (_ :guard seq?))
           :features-mask (:or (_ :guard vector?)
                               (_ :guard seq?))
           :labels-mask (:or (_ :guard vector?)
                             (_ :guard seq?))}]
         `(doto ~mln
           (.fit (vec-or-matrix->indarray ~features)
                 (vec-or-matrix->indarray ~labels)
                 (vec-or-matrix->indarray ~features-mask)
                 (vec-or-matrix->indarray ~labels-mask)))
         [{:mln _ :features _ :labels _ :features-mask _ :labels-mask _}]
         (doto mln
           (.fit (vec-or-matrix->indarray features)
                 (vec-or-matrix->indarray labels)
                 (vec-or-matrix->indarray features-mask)
                 (vec-or-matrix->indarray labels-mask)))
         [{:mln (_ :guard seq?)
           :examples (:or (_ :guard vector?)
                          (_ :guard seq?))
           :label-idxs (:or (_ :guard vector?)
                            (_ :guard seq?))}]
         `(doto ~mln
           (.fit (vec-or-matrix->indarray ~examples)
                 (int-array ~label-idxs)))
         [{:mln _ :examples _ :label-idxs _}]
         (doto mln
           (.fit (vec-or-matrix->indarray examples)
                 (int-array label-idxs)))
         [{:mln (_ :guard seq?)
           :data (:or (_ :guard vector?)
                      (_ :guard seq?))
           :labels (:or (_ :guard vector?)
                        (_ :guard seq?))}]
         `(doto ~mln
            (.fit (vec-or-matrix->indarray ~data) (vec-or-matrix->indarray ~labels)))
         [{:mln _ :data _ :labels _}]
         (doto mln
           (.fit (vec-or-matrix->indarray data) (vec-or-matrix->indarray labels)))
         [{:mln (_ :guard seq?)
           :data (:or (_ :guard vector?)
                      (_ :guard seq?))}]
         `(doto ~mln
            (.fit (vec-or-matrix->indarray ~data)))
         [{:mln _ :data _}]
         (doto mln
           (.fit (vec-or-matrix->indarray data)))
         [{:mln (_ :guard seq?) :iter (_ :guard seq?)}]
         `(doto ~mln
            (.fit ~iter))
         [{:mln _ :iter _}]
         (doto mln
           (.fit (reset-if-empty?! iter)))
         [{:mln (_ :guard seq?) :dataset (_ :guard seq?)}]
         `(doto ~mln (.fit ~dataset))
         [{:mln _ :dataset _}]
         (doto mln (.fit dataset))
         [{:mln (_ :guard seq?)}]
         `(doto ~mln .fit)
         :else
         (doto mln .fit)))

;; add a basic example here in a single fn to illistrate the flow
;; add in printlns and listeners and everything

;; look into refactoring with list*
;; https://clojuredocs.org/clojure.core/list*



;; for local training, this is one of:
;; early-stopping-trainer, classifier, mln

;; are are:
;; just-this, dataset/iter/examples/labels,
;; dataset/iter/data/labels/features/features-mask/labels-mask/examples/label-idxs

;; (.fit trainer)
;; (doto classifier (.fit dataset))
;; (doto classifier (.fit (reset-iterator! iter)))
;; (doto classifier (.fit (vec-or-matrix->indarray examples)
;;                        (vec-or-matrix->indarray labels)))



#_(doto mln
    (.fit (vec-or-matrix->indarray features)
          (vec-or-matrix->indarray labels)
          (vec-or-matrix->indarray features-mask)
          (vec-or-matrix->indarray labels-mask)))
#_(doto mln
    (.fit (vec-or-matrix->indarray examples)
          (int-array label-idxs)))

#_(doto mln
    (.fit (vec-or-matrix->indarray data) (vec-or-matrix->indarray labels)))

#_(doto mln
    (.fit (vec-or-matrix->indarray data)))

#_(doto mln
    (.fit (reset-if-empty?! iter)))

#_(doto mln (.fit dataset))

#_(doto mln .fit)












































;;TODO

;;testing data https://archive.ics.uci.edu/ml/machine-learning-databases/poker/poker-hand-testing.data
;;training data https://archive.ics.uci.edu/ml/machine-learning-databases/poker/poker-hand-training-true.data
;;desc https://archive.ics.uci.edu/ml/machine-learning-databases/poker/poker-hand.names
;; attributes in data
;; pairs of suit, rank of card (5 cards = 10 pairs)
;; last value is class of poker hand
;; 0-9

;; set up the evaluator
(comment


  (defn timestamp-now
    "Returns a string timestamp in the form of 2015-10-02T18:27:43Z."
    []
    (str (tf/unparse (tf/formatters :date-hour-minute-second)
                     (t/now))
         "Z"))

  (defn initialize-record-reader
    [record-reader file-path]
    (doto record-reader
      (rr/initialize (f/new-filesplit {:root-dir file-path}))))

  (defn set-up-data-set-iterator
    [record-reader batch-size label-idx num-diff-labels]
    (ds/iterator {:rr-dataset-iter {:record-reader record-reader
                                    :batch-size batch-size
                                    :label-idx label-idx
                                    :n-possible-labels num-diff-labels}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; import the data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  (def fresh-csv-rr (rr/record-reader {:csv-rr {}}))

  (def initialized-training-rr
    (initialize-record-reader fresh-csv-rr "resources/poker/poker-hand-training.csv"))

  (def initialized-testing-rr
    (initialize-record-reader fresh-csv-rr "resources/poker/poker-hand-testing.csv"))

  (def training-iter
    (set-up-data-set-iterator initialized-training-rr 25 10 10))

  (def testing-iter
    (set-up-data-set-iterator initialized-testing-rr 25 10 10))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; set up network
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  (def conf
    ;; play around with regularization (L1, L2, dropout ...)
    (->
     (.build
      (nn-conf/nn-conf-builder {:seed 123
                                :optimization-algo :stochastic-gradient-descent
                                :iterations 1
                                :learning-rate 0.006
                                :updater :nesterovs
                                :momentum 0.9
                                :pre-train false
                                :backprop true
                                :layers {0 {:dense-layer {:n-in 10
                                                          :n-out 30
                                                          :weight-init :xavier
                                                          :activation-fn :relu}}
                                         1 {:output-layer {:n-in 30
                                                           :loss-fn :negativeloglikelihood
                                                           :weight-init :xavier
                                                           :activation-fn :soft-max
                                                           :n-out 10}}}}))
     (mlb/multi-layer-config-builder {})))

  (def model (mln/multi-layer-network conf))

  (defn init [mln]
    ;;.init is going to be implemented in dl4clj.nn.multilayer.multi-layer-network
    (doto mln
      .init))

  (def init-model (init model))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; train-model
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  (def numepochs 10)

  (defn ex-train [mln]
    (loop
        [i 0
         result {}]
      (cond (not= i numepochs)
            (do
              (println "current at epoch:" i)
              (recur (inc i)
                     (.fit mln training-iter)))
            ;;.fit is going to be implemented in dl4clj.nn.multilayer.multi-layer-network
            (= i numepochs)
            (do
              (println "training done")
              mln))))

  (def trained-model (ex-train init-model))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; evaluate model
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  (def evaler-c (e/new-evaluator {:n-classes 10}))

  (defn eval-model [mln]
    (while (true? (.hasNext testing-iter))
      (let [nxt (.next testing-iter)
            output (.output mln (.getFeatureMatrix nxt))]
        (do (.eval e (.getLabels nxt) output)
            (println (.stats e))))))

  #_(defn eval-model-classification
      [mln testing-iter evaler]
      (while (true? (rr/has-next? testing-iter))
        (let [next (rr/next-data-record testing-iter)
              output (.output mln (d/get-feature-matrix nxt))]
          ;;.output is going to be implemented in dl4clj.nn.multilayer.multi-layer-network
          (do (e/eval-classification
               evaler
               (rr/get-labels next)
               output)
              (println (e/get-stats evaler))))
        ))

  (def evaled-model (eval-model trained-model))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; graveyard
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


  (def num-lines-to-skip 0)
  (def delim ",")
  (def base-dir "/Users/will/projects/dl4clj/resources/")
  (def file-name "poker-hand-training.txt")
  (def input-path (str base-dir file-name))
  (def output-path (str base-dir "reports_processed_" (timestamp-now)))

  (def input-schema-old
    (.build
     (doto (Schema$Builder. )
       (.addColumnCategorical "suit-c1" '("1" "2" "3" "4"))
       (.addColumnLong "card-rank-1" 1 13) ;;first card
       (.addColumnCategorical "suit-c2" '("1" "2" "3" "4"))
       (.addColumnLong "card-rank-2" 1 13) ;; second card
       (.addColumnCategorical "suit-c3" '("1" "2" "3" "4"))
       (.addColumnLong "card-rank-3" 1 13) ;; third card
       (.addColumnCategorical "suit-c4" '("1" "2" "3" "4"))
       (.addColumnLong "card-rank-4" 1 13) ;; 4th card
       (.addColumnCategorical "suit-c5" '("1" "2" "3" "4"))
       (.addColumnLong "card-rank-5" 1 13) ;; 5th card
       (.addColumnCategorical "hand-value" '("0" "1" "2" "3" "4" "5" "6" "7" "8" "9"))
       )))

  (def experimental-schema
    (.build
     (doto (Schema$Builder.)
       (.addColumnsLong "everything" 0 11))))

  (def data-transform
    (.build
     (doto (TransformProcess$Builder. experimental-schema)
       (.categoricalToInteger (into-array String "everything") #_(into-array String '("suit-c1" "suit-c2" "suit-c3"
                                                                                      "suit-c4" "suit-c5" "hand-value"))))))

  (def experimental-da)

  (defn check-data-transform
    [dt]
    (let [num-actions (.size (.getActionList dt))]
      (loop [i 0]
        (cond (not= i num-actions)
              (do
                (println (str "Step " i ": " (.get (.getActionList dt) i) "\n"))
                (println (str "This is what the data now looks like: "
                              (.getSchemaAfterStep dt i)))
                (recur (inc i)))
              (= i num-actions)
              (println "no more transforms")))))

  (check-data-transform data-transform)
  (.get (.getActionList data-transform) 0)
  #_(def new-spark-conf (SparkConf. ))

  #_(def spark-conf
      (doto new-spark-conf
        (.setMaster "local[*]")
        (.setAppName "poker hand classification")))

  #_(def spark-context (JavaSparkContext. spark-conf))

  #_(.textFile spark-context "resources/poker-spark-test.csv")

  ;; use spark to read the data
  (def lines (.textFile spark-context input-path))

  #_(.textFile spark-context input-path)

  #_(.textFile spark-context )
  ;; convert to writable

  ;; need our writeable fn
  (def writeable-fn (StringToWritablesFunction. (CSVRecordReader.)))

  (def poker-hands
    (doto lines
      (.map writeable-fn)))
  (.map  lines (StringToWritablesFunction. (CSVRecordReader.)))
  ( poker-hands)
  (type (Strun-ringToWritablesFunction. (CSVRecordReader.)))
  (type poker-hands)
  (type data-transform)

  (.getFinalSchema data-transform)
  (.getActionList data-transform)
  (def processed (SparkTransformExecutor/execute poker-hands data-transform))
  (SparkTransformExecutor/executeSequenceToSeparate poker-hands data-transform)
  (.first poker-hands)
  (.execute  poker-hands (.build
                          (.categoricalToInteger
                           (TransformProcess$Builder.
                            (.build
                             (-> (Schema$Builder. ))

                             (.addColumnCategorical "suit-c1" '("1" "2" "3" "4"))))
                           (into-array String (list "suit-c1")))))
  (.executeToSequence poker-hands data-transform)
  )


(comment
  "java still faster :()"
 (use '[dl4clj.nn.conf.builders.nn-conf-builder])
(use '[dl4clj.nn.conf.builders.multi-layer-builders])
(use '[dl4clj.datasets.iterator.impl.default-datasets])
(use '[dl4clj.optimize.listeners.listeners])
(use '[dl4clj.nn.multilayer.multi-layer-network])
(use '[dl4clj.nn.api.model])
(use '[dl4clj.helpers])

(def train-mnist-iter (new-mnist-data-set-iterator :batch-size 64 :train? true :seed 123))

#_(reset-if-not-at-start! train-mnist-iter)

(def lazy-l-builder (nn-conf-builder
                :optimization-algo :stochastic-gradient-descent
                :seed 123 :iterations 1 :default-activation-fn :relu
                :regularization? true :default-l2 7.5e-6
                :default-weight-init :xavier :default-learning-rate 0.0015
                :default-updater :nesterovs :default-momentum 0.98
                :layers {0 {:dense-layer
                            {:layer-name "example first layer"
                             :n-in 784 :n-out 500}}
                         1 {:dense-layer
                            {:layer-name "example second layer"
                             :n-in 500 :n-out 100}}
                         2 {:output-layer
                            {:n-in 100 :n-out 10 :loss-fn :negativeloglikelihood
                             :activation-fn :softmax
                             :layer-name "example output layer"}}}))

(def lazy-multi-layer-conf
  (multi-layer-config-builder
   :list-builder lazy-l-builder
   :backprop? true
   :pretrain? false))

(def multi-layer-network-lazy-training
  (init! :model (new-multi-layer-network :conf lazy-multi-layer-conf)))

(def lazy-score-listener (new-score-iteration-listener :print-every-n 5))


(def mln-lazy-with-listener (set-listeners! :model multi-layer-network-lazy-training
                                            :listeners [lazy-score-listener]))

(def lazy-data (data-from-iter train-mnist-iter))


#_(time
 (train-mln-with-lazy-seq! :lazy-seq-data lazy-data :mln mln-lazy-with-listener
                           :n-epochs 15))

;; => "Elapsed time: 262699.667516 msecs"






(def l-builder (nn-conf-builder
                     :optimization-algo :stochastic-gradient-descent
                     :seed 123 :iterations 1 :default-activation-fn :relu
                     :regularization? true :default-l2 7.5e-6
                     :default-weight-init :xavier :default-learning-rate 0.0015
                     :default-updater :nesterovs :default-momentum 0.98
                     :layers {0 {:dense-layer
                                 {:layer-name "example first layer"
                                  :n-in 784 :n-out 500}}
                              1 {:dense-layer
                                 {:layer-name "example second layer"
                                  :n-in 500 :n-out 100}}
                              2 {:output-layer
                                 {:n-in 100 :n-out 10 :loss-fn :negativeloglikelihood
                                  :activation-fn :softmax
                                  :layer-name "example output layer"}}}))

(def multi-layer-conf
  (multi-layer-config-builder
   :list-builder l-builder
   :backprop? true
   :pretrain? false))

(def multi-layer-network-standard-training
  (init! :model (new-multi-layer-network :conf multi-layer-conf)))


(def score-listener (new-score-iteration-listener :print-every-n 5))

(def mln-standard-with-listener (set-listeners! :model multi-layer-network-standard-training
                                                :listeners [score-listener]))

#_(def trained-mln (time (train-mln-with-ds-iter! :mln mln-standard-with-listener
                                          :ds-iter train-mnist-iter
                                          :n-epochs 15)))
;; => "Elapsed time: 220454.276825 msecs"
)

(comment
 (def poker-path "resources/poker-hand-training.csv")

(def file-split (s/new-filesplit :path poker-path))

(def csv-rr (initialize-rr! :rr (rr/new-csv-record-reader :skip-num-lines 0 :delimiter ",")
                            :input-split file-split))

(println (next-record! csv-rr))

(reset-rr! csv-rr)

(def rr-ds-iter (ds-iter/new-record-reader-dataset-iterator
                 :record-reader csv-rr ;; no need to reset manually, done for you behind the scene
                 :batch-size 1
                 :label-idx 10
                 :n-possible-labels 10))

(def other-rr-ds-iter (ds-iter/new-record-reader-dataset-iterator
                       :record-reader csv-rr
                       :batch-size 1
                       :label-idx -1
                       :n-possible-labels 10))

(str (next-example! rr-ds-iter))

(= (next-example! (reset-iter! rr-ds-iter))
   (next-example! (reset-iter! other-rr-ds-iter)))

(def lazy-seq-data (data-from-iter (reset-iter! rr-ds-iter)))

(def lazy-iter (.iterator lazy-seq-data))

(realized? lazy-seq-data)

(first lazy-seq-data)

(defn reset-iterator!
  "resets an iterator"
  [iter]
  (try (reset-iter! iter)
       (catch Exception e iter)))

(def example-array (vec-or-matrix->indarray [1 2 3 4]))

[nd4clj.linalg.factory.nd4j :refer [vec-or-matrix->indarray]]

(println (vec-or-matrix->indarray example-array))
)








;; spark testing
;; update this to tuse the mnist example
;; see if you can get same score as regular training

#_(ns my.ns
  (:require [dl4clj.nn.conf.builders.layers :as l]
            [dl4clj.nn.conf.builders.nn :as nn-conf]
            [dl4clj.nn.multilayer.multi-layer-network :as mln]
            [dl4clj.datasets.iterators :refer [new-iris-data-set-iterator]]
            [dl4clj.eval.api.eval :refer [get-stats]]
            [dl4clj.spark.masters.param-avg :as master]
            [dl4clj.spark.data.java-rdd :refer [new-java-spark-context java-rdd-from-iter]]
            [dl4clj.spark.dl4j-multi-layer :as spark-mln]
            [clojure.core.match :refer [match]]
            [dl4clj.helpers :refer [data-from-iter]]
            [dl4clj.nn.training :refer [train-with-spark]]
            [dl4clj.spark.api.dl4j-multi-layer :refer [fit-spark-mln! eval-classification-spark-mln
                                                       get-spark-context get-score]]))

#_(def mln-conf
  (nn-conf/builder
   :optimization-algo :stochastic-gradient-descent
   :default-learning-rate 0.006
   :layers {0 (l/dense-layer-builder :n-in 4 :n-out 2 :activation-fn :relu)
            1 {:output-layer
               {:loss-fn :negativeloglikelihood
                :n-in 2 :n-out 3
                :activation-fn :soft-max
                :weight-init :xavier}}}
   :backprop? true
   :backprop-type :standard))

#_(def training-master
  (master/new-parameter-averaging-training-master
   :build? true :rdd-n-examples 10 :n-workers 3 :averaging-freq 10
   :batch-size-per-worker 2 :export-dir "resources/spark/master/"
   :rdd-training-approach :direct :repartition-data :always
   :repartition-strategy :balanced :seed 1234 :save-updater? true
   :storage-level :none :as-code? true))

#_(def your-spark-context
  (new-java-spark-context :app-name "example app" :as-code? true))

#_(def iris-iter (new-iris-data-set-iterator :batch-size 2 :n-examples 10))

#_(def fit-spark-mln (train-with-spark :spark-context your-spark-context
                                     :mln-conf mln-conf
                                     :training-master training-master
                                     :iter iris-iter
                                     :n-epochs 4
                                     :as-code? false))


#_(get-score :spark-mln fit-spark-mln)


;; This buffer is for Clojure experiments and evaluation.
;; Press C-j to evaluate the last expression.
;; both ways work, havnt figured out the difference in n-epochs from
;; what spark is doing

(ns my.ns
  (:require [dl4clj.nn.conf.builders.layers :as l]
            [dl4clj.nn.conf.builders.nn :as nn-conf]
            [dl4clj.nn.multilayer.multi-layer-network :as mln]
            [dl4clj.datasets.iterators :refer [new-mnist-data-set-iterator]]
            [dl4clj.eval.api.eval :refer [get-stats]]
            [dl4clj.spark.masters.param-avg :as master]
            [dl4clj.spark.data.java-rdd :refer [new-java-spark-context java-rdd-from-iter]]
            [dl4clj.spark.dl4j-multi-layer :as spark-mln]
            [clojure.core.match :refer [match]]
            [dl4clj.helpers :refer [data-from-iter]]
            [dl4clj.nn.training :refer [train-with-spark]]
            [dl4clj.spark.api.dl4j-multi-layer :refer [fit-spark-mln! eval-classification-spark-mln
                                                       get-spark-context get-score]]))


(def your-spark-context
  (new-java-spark-context :app-name "example app" :as-code? true))

(def mln-conf
  (nn-conf/builder
   :optimization-algo :stochastic-gradient-descent
   :iterations 1
   :default-activation-fn :leaky-relu
   :default-weight-init :xavier
   :default-learning-rate 0.02
   :default-updater :nesterovs
   :default-momentum 0.9
   :regularization? true
   :default-l2 1e-4
   :layers {0 (l/dense-layer-builder :n-in 784 :n-out 500)
            1 (l/dense-layer-builder :n-in 500 :n-out 100)
            2 {:output-layer
               {:loss-fn :negativeloglikelihood
                :n-in 100  :n-out 10
                :activation-fn :soft-max
                :weight-init :xavier}}}
   :backprop? true
   :pretrain? false))

(def training-master
  (master/new-parameter-averaging-training-master
   :worker-prefetch-n-batches 2
   :rdd-n-examples 16
   :averaging-freq 5
   :batch-size-per-worker 16
   :export-dir "resources/spark/master/readme/"
   :as-code? true))

(def train-mnist-iter (new-mnist-data-set-iterator :batch-size 16 :train? true :seed 12345))

(def test-mnist-iter (new-mnist-data-set-iterator :batch-size 16 :train? false :seed 12345
                                                  :as-code? false))

(def fit-spark-mln (train-with-spark :spark-context your-spark-context
                                     :mln-conf mln-conf
                                     :training-master training-master
                                     :iter train-mnist-iter
                                     :n-epochs 2
                                     :as-code? false))
;; should be 54 epochs, only got to 51
;; got similar evaluation stats
;; lets go again and see what we get
;; this time 49

;; switching back to loop to see what it gives
;; got 52
;; got 54

;; all results are similar
(defn eval-class-spark
  [& {:keys [spark-mln iter as-code?]
      :or {as-code? true}}]
  (let [sc (get-spark-context :spark-mln spark-mln)
        rdd (java-rdd-from-iter :spark-context sc :iter iter)]
    (eval-classification-spark-mln :spark-mln spark-mln :rdd rdd)))

(println (get-stats :evaler (eval-class-spark :spark-mln fit-spark-mln :iter test-mnist-iter)))



(defn test-loop
  [n-epochs]
  (loop [i 0
         result {}]
    (cond (not= i n-epochs)
          (do
            (println "current at epoch:" i)
            (recur (inc i)
                   "foo"))
          (= i n-epochs)
          (do
            (println "training done")
            result))
    ))
(test-loop 2)

(defn test-dotimes
  [n-epochs]
 (do
  (dotimes [n n-epochs]
    (println (+ 1 n)))
  (println "done")))

(test-dotimes 2)
