(ns ^{:doc "implementation of the eval class in dl4j.  Used to get performance metrics for a model
see: https://deeplearning4j.org/doc/org/deeplearning4j/eval/Evaluation.html and
https://deeplearning4j.org/doc/org/deeplearning4j/eval/RegressionEvaluation.html"}
    dl4clj.eval.evaluation
  (:import [org.deeplearning4j.eval Evaluation RegressionEvaluation BaseEvaluation])
  (:require [dl4clj.utils :refer [generic-dispatching-fn]]
            [clojure.core.match :refer [match]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; multimethod for creating the evaluation java object
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti evaler generic-dispatching-fn)

(defmethod evaler :classification [opts]
  (let [conf (:classification opts)
        {labels :labels
         top-n :top-n
         l-to-i-map :label-to-idx
         n-classes :n-classes} conf]
    (match [conf]
           [{:labels _ :top-n _}]
           `(Evaluation. ~labels ~top-n)
           [{:labels _}]
           `(Evaluation. (into () ~labels))
           [{:label-to-idx _}]
           `(Evaluation. ~l-to-i-map)
           [{:n-classes _}]
           `(Evaluation. ~n-classes)
           :else
           `(Evaluation.))))

(defmethod evaler :regression [opts]
  (let [conf (:regression opts)
        {column-names :column-names
         precision :precision
         n-columns :n-columns} conf]
    (match [conf]
           [{:column-names _ :precision _}]
           `(RegressionEvaluation. (into () ~column-names) ~precision)
           [{:n-columns _ :precision _}]
           `(RegressionEvaluation. ~n-columns ~precision)
           [{:column-names _}]
           `(RegressionEvaluation. (into () ~column-names))
           [{:n-columns _}]
           `(RegressionEvaluation. ~n-columns))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; user facing fns
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn new-classification-evaler
  "Creates an instance of an evaluation object which reports precision, recall, f1

   :labels (coll), a collection of string labels to use for the output

   :top-n (int), value to use for the top N accuracy calc.
     - An example is considered correct if the probability for the true class
       is one of the highest n values

   :n-classes (int), the number of classes to account for in the evaluation

   :label-to-idx (map), {column-idx (int) label (str)}
    - another way to set the labels for the classification

   :as-code? (boolean), return the java object or the code for creating the object"
  [& {:keys [labels top-n label-to-idx n-classes as-code?]
      :or {as-code? true}
      :as opts}]
  (let [code (evaler {:classification opts})]
    (if as-code?
      code
      (eval code))))

(defn new-regression-evaler
  "Evaluation method for the evaluation of regression algorithms.

   provides MSE, MAE, RMSE, RSE, correlation coefficient for each column

   :column-names (coll), a collection of string naming the columns

   :precision (int), specified precision to be used

   :n-columns (int), the number of columns in the dataset

   :as-code? (boolean), return the java object or the code for creating it"
  [& {:keys [column-names precision n-columns as-code?]
      :or {as-code? true}
      :as opts}]
  (let [code (evaler {:regression opts})]
    (if as-code?
      code
      (eval code))))
