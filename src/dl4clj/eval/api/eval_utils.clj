(ns ^{:doc "Utility fns for performing evaluation.
 see: https://deeplearning4j.org/doc/org/deeplearning4j/eval/EvaluationUtils.html"}
    dl4clj.eval.api.eval-utils
  (:import [org.deeplearning4j.eval EvaluationUtils])
  (:require [nd4clj.linalg.factory.nd4j :refer [vec-or-matrix->indarray]]
            [clojure.core.match :refer [match]]))

(defn extract-non-masked-time-steps
  "returns the original time series given the labels, the output of a model
  and the mask applied to that output"
  [& {:keys [labels predicted output-mask]
      :as opts}]
  (match [opts]
         [{:labels (:or (_ :guard vector?)
                        (_ :guard seq?))
           :predicted (:or (_ :guard vector?)
                           (_ :guard seq?))
           :output-mask (:or (_ :guard vector?)
                             (_ :guard seq?))}]
         `(EvaluationUtils/extractNonMaskedTimeSteps
          (vec-or-matrix->indarray ~labels)
          (vec-or-matrix->indarray ~predicted)
          (vec-or-matrix->indarray ~output-mask))
         :else
         (EvaluationUtils/extractNonMaskedTimeSteps
          (vec-or-matrix->indarray labels)
          (vec-or-matrix->indarray predicted)
          (vec-or-matrix->indarray output-mask))))

(defn reshape-time-series-to-2d
  "reshapes a time series to be two dimensional"
  [labels]
  (match [labels]
         [(:or (_ :guard vector?)
               (_ :guard seq?))]
         `(EvaluationUtils/reshapeTimeSeriesTo2d (vec-or-matrix->indarray ~labels))
         :else
         (EvaluationUtils/reshapeTimeSeriesTo2d (vec-or-matrix->indarray labels))))