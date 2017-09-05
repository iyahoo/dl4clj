(ns ^{:doc "fns from the dl4j Interface for a layer of a neural network.
 This has an activation function, an input and output size, weights, and a bias
 see http://deeplearning4j.org/doc/org/deeplearning4j/nn/api/Layer.html"}
  dl4clj.nn.api.layer
  (:import [org.deeplearning4j.nn.api Layer])
  (:require [dl4clj.utils :refer [contains-many?]]
            [dl4clj.constants :as enum]
            [clojure.core.match :refer [match]]
            [nd4clj.linalg.factory.nd4j :refer [vec-or-matrix->indarray]]))

(defn initializer
  "returns a param initializer for this layer"
  [layer]
  (match [layer]
         [(_ :guard seq?)]
         `(.initializer ~layer)
         :else
         (.initializer layer)))

(defn instantiate
  "returns the instantiated layer

  :layer (layer), the layer set in the nn-conf

  :conf (nn-conf), a single layer neural network configuration

  :listener (coll), a collection of listeners for the layer

  :layer-idx (int), the index of the layer within the model

  :layer-param-view (INDArray or vec), the params of the layer

  :initialize-params? (boolean), do you want to initialize the params?"
  [& {:keys [layer conf listener layer-idx
             layer-param-view initialize-params?]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :conf (_ :guard seq?)
           :listener _
           :layer-idx (:or (_ :guard number?)
                           (_ :guard seq?))
           :layer-param-view (:or (_ :guard vector?)
                                  (_ :guard seq?))
           :initialize-params? (:or (_ :guard boolean?)
                                    (_ :guard seq?))}]
         `(.instantiate ~layer ~conf ~listener (int ~layer-idx)
                        (vec-or-matrix->indarray ~layer-param-view)
                        ~initialize-params?)
         :else
         (.instantiate layer conf listener layer-idx
                       (vec-or-matrix->indarray layer-param-view)
                       initialize-params?)))

(defn activate-layer
  "6 opts for triggering an activation
  1) only supply the layer, Trigger an activation with the last specified input

  2) supply training? (boolean), trigger with the last specified input

  3) supply input (INDArray or vec), initialize the layer with the given input and return
   the activation for this layer given this input

  4) supply training-mode (keyword), Trigger an activation with the last specified input
    - keyword is one of :test or :train

  5) supply input and training?

  6) supply input and training-mode
   -both 4 and 5 initialize the layer with the given input and return the activation for this layer given this input"
  [& {:keys [model training? input training-mode]
      :as opts}]
  (match [opts]
         [{:model (_ :guard seq?)
           :input (:or (_ :guard vector?)
                       (_ :guard seq?))
           :training? (:or (_ :guard boolean?)
                           (_ :guard seq?))}]
         `(.activate ~model (vec-or-matrix->indarray ~input) ~training?)
         [{:model _ :input _ :training? _}]
         (.activate model (vec-or-matrix->indarray input) training?)
         [{:model (_ :guard seq?)
           :input (:or (_ :guard vector?)
                       (_ :guard seq?))
           :training-mode (:or (_ :guard keyword?)
                               (_ :guard seq?))}]
         `(.activate ~model
                     (vec-or-matrix->indarray ~input)
                     (enum/value-of {:layer-training-mode ~training-mode}))
         [{:model _ :input _ :training-mode _}]
         (.activate model (vec-or-matrix->indarray input) (enum/value-of {:layer-training-mode training-mode}))
         [{:model (_ :guard seq?)
           :input (:or (_ :guard vector?)
                       (_ :guard seq?))}]
         `(.activate ~model (vec-or-matrix->indarray ~input))
         [{:model _ :input _}]
         (.activate model (vec-or-matrix->indarray input))
         [{:model (_ :guard seq?)
           :training? (:or (_ :guard boolean?)
                           (_ :guard seq?))}]
         `(.activate ~model ~training?)
         [{:model _ :training? _}]
         (.activate model training?)
         [{:model (_ :guard seq?)
           :training-mode (:or (_ :guard keyword?)
                               (_ :guard seq?))}]
         `(.activate ~model (enum/value-of {:layer-training-mode ~training-mode}))
         [{:model _ :training-mode _}]
         (.activate model (enum/value-of {:layer-training-mode training-mode}))
         [{:model (_ :guard seq?)}]
         `(.activate ~model)
         :else
         (.activate model)))

(defn layer-output
  "returns the output of a layer.

  :input (INDArray or vec), the input to the layer

  :training? (boolean), are we in trianing mode or testing?

  multiple layers implement this fn"
  [& {:keys [layer input training?]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :input (:or (_ :guard vector?)
                       (_ :guard seq?))
           :training? (:or (_ :guard boolean?)
                           (_ :guard seq?))}]
         `(.output ~layer (vec-or-matrix->indarray ~input) ~training?)
         [{:layer _ :input _ :training? _}]
         (.output layer (vec-or-matrix->indarray input) training?)
         [{:layer (_ :guard seq?)
           :input (:or (_ :guard vector?)
                       (_ :guard seq?))}]
         `(.output ~layer (vec-or-matrix->indarray ~input))
         [{:layer _ :input _ }]
         (.output layer (vec-or-matrix->indarray input))
         [{:layer (_ :guard seq?)
           :training? (:or (_ :guard boolean?)
                           (_ :guard seq?))}]
         `(.output ~layer ~training?)
         [{:layer _ :training? _}]
         (.output layer training?)))

(defn feed-forward-mask-array
  "Feed forward the input mask array, setting in in the layer as appropriate.

   :mask-array (INDArray or vec of mask values),

   :mask-state (keyword), either :active or :passthrough
    - :active = apply mask to activations and errors.
    - :passthrough = feed forward the input mask (if/when necessary) but don't actually apply it.
    - Note: Masks should not be applied in all cases, depends on the network configuration

  :batch-size (int) the minibatch size to use"
  [& {:keys [layer mask-array mask-state batch-size]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :mask-array (:or (_ :guard vector?)
                            (_ :guard seq?))
           :mask-state (:or (_ :guard keyword?)
                            (_ :guard seq?))
           :batch-size (:or (_ :guard number?)
                            (_ :guard seq?))}]
         `(.feedForwardMaskArray ~layer (vec-or-matrix->indarray ~mask-array)
                                 (enum/value-of {:mask-state ~mask-state})
                                 (int ~batch-size))
         :else
         (.feedForwardMaskArray layer (vec-or-matrix->indarray mask-array)
                                (enum/value-of {:mask-state mask-state})
                                batch-size)))

(defn backprop-gradient
  "Calculate the gradient relative to the error in the next layer
   epsilon is an INDArray or vec"
  [& {:keys [layer epsilon]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :epsilon (:or (_ :guard vector?)
                         (_ :guard seq?))}]
         `(.backpropGradient ~layer (vec-or-matrix->indarray ~epsilon))
         :else
         (.backpropGradient layer (vec-or-matrix->indarray epsilon))))

(defn calc-l1
  "Calculate the l1 regularization term. 0.0 if regularization is not used."
  [& {:keys [layer backprop-only-params?]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :backprop-only-params? (:or (_ :guard boolean?)
                                       (_ :guard seq?))}]
         `(.calcL1 ~layer ~backprop-only-params?)
         :else
         (.calcL1 layer backprop-only-params?)))

(defn calc-l2
  "Calculate the l2 regularization term. 0.0 if regularization is not used."
  [& {:keys [layer backprop-only-params?]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :backprop-only-params? (:or (_ :guard boolean?)
                                       (_ :guard seq?))}]
         `(.calcL2 ~layer ~backprop-only-params?)
         :else
         (.calcL2 layer backprop-only-params?)))

(defn get-index
  "Get the layer index."
  [layer]
  (match [layer]
         [(_ :guard seq?)]
         `(.getIndex ~layer)
         :else
         (.getIndex layer)))

(defn get-input-mini-batch-size
  "Get current/last input mini-batch size, as set by set-input-mini-batch-size!"
  [layer]
  (match [layer]
         [(_ :guard seq?)]
         `(.getInputMiniBatchSize ~layer)
         :else
         (.getInputMiniBatchSize layer)))

(defn get-listeners
  "Get the iteration listeners for this layer."
  [layer]
  (match [layer]
         [(_ :guard seq?)]
         `(.getListeners ~layer)
         :else
         (.getListeners layer)))

(defn get-mask-array
  "get the mask array"
  [layer]
  (match [layer]
         [(_ :guard seq?)]
         `(.getMaskArray ~layer)
         :else
         (.getMaskArray layer)))

(defn is-pretrain-layer?
  "Returns true if the layer can be trained in an unsupervised/pretrain manner (VAE, RBMs etc)"
  [layer]
  (match [layer]
         [(_ :guard seq?)]
         `(.isPretrainLayer ~layer)
         :else
         (.isPretrainLayer layer)))

(defn pre-output
  "returns the raw activations for the supplied layer

  :input (INDArray or vec), the input to the layer

  :training? (boolean), are we in training or testing mode?

  :training-mode (keyword), are we in training or testing mode?
   one of :training or :testing

  multiple layers implement this fn"
  [& {:keys [layer input training? training-mode]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :input (:or (_ :guard vector?)
                       (_ :guard seq?))
           :training? (:or (_ :guard boolean?)
                           (_ :guard seq?))}]
         `(.preOutput ~layer (vec-or-matrix->indarray ~input) ~training?)
         [{:layer _ :input _ :training? _}]
         (.preOutput layer (vec-or-matrix->indarray input) training?)
         [{:layer (_ :guard seq?)
           :input (:or (_ :guard vector?)
                       (_ :guard seq?))
           :training-mode (:or (_ :guard keyword?)
                               (_ :guard seq?))}]
         `(.preOutput ~layer
                      (vec-or-matrix->indarray ~input)
                      (enum/value-of {:training-mode ~training-mode}))
         [{:layer _ :input _ :training-mode _}]
         (.preOutput layer
                     (vec-or-matrix->indarray input)
                     (enum/value-of {:training-mode training-mode}))
         [{:layer (_ :guard seq?)
           :training? (:or (_ :guard boolean?)
                           (_ :guard seq?))}]
         `(.preOutput ~layer ~training?)
         [{:layer _ :training? _}]
         (.preOutput layer training?)
         [{:layer (_ :guard seq?)
           :input (:or (_ :guard seq?)
                       (_ :guard vector?))}]
         `(.preOutput ~layer (vec-or-matrix->indarray ~input))
         [{:layer _ :input _ }]
         (.preOutput layer (vec-or-matrix->indarray input))))

(defn set-index!
  "Set the layer index and returns the layer.
  :index should be an integer"
  [& {:keys [layer index]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :index (:or (_ :guard number?)
                       (_ :guard seq?))}]
         `(doto ~layer
            (.setIndex (int ~index)))
         :else
         (doto layer
           (.setIndex (int index)))))

(defn set-layer-input!
  "set the layer's input and return the layer"
  [& {:keys [layer input]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :input (:or (_ :guard seq?)
                       (_ :guard vector?))}]
         `(doto ~layer
            (.setInput (vec-or-matrix->indarray ~input)))
         :else
         (doto layer
           (.setInput (vec-or-matrix->indarray input)))))

(defn set-input-mini-batch-size!
  "Set current/last input mini-batch size and return the layer.
  Used for score and gradient calculations."
  [& {:keys [layer size]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :size (:or (_ :guard number?)
                      (_ :guard seq?))}]
         `(doto ~layer
            (.setInputMiniBatchSize (int ~size)))
         :else
         (doto layer
           (.setInputMiniBatchSize size))))

(defn set-layer-listeners!
  "Set the iteration listeners for the supplied layer and returns the layer.
  listeners is a collection or array of iteration listeners"
  [& {:keys [layer listeners]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :listeners _}]
         `(doto ~layer
            (.setListeners ~listeners))
         :else
         (doto layer
           (.setListeners listeners))))

(defn set-mask-array!
  "Set the mask array for this layer and return the layer
  mask-array is an INDArray"
  [& {:keys [layer mask-array]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :mask-array (:or (_ :guard vector?)
                            (_ :guard seq?))}]
         `(doto ~layer
            (.setMaskArray (vec-or-matrix->indarray ~mask-array)))
         :else
         (doto layer
           (.setMaskArray (vec-or-matrix->indarray mask-array)))))

(defn transpose
  "Return a transposed copy of the weights/bias
  (this means reverse the number of inputs and outputs on the weights)"
  [layer]
  (match [layer]
         [(_ :guard seq?)]
         `(.transpose ~layer)
         :else
         (.transpose layer)))

(defn get-layer-type
  "Returns the layer type"
  [layer]
  (match [layer]
         [(_ :guard seq?)]
         `(.type ~layer)
         :else
         (.type layer)))

(defn get-l1-by-param-layer
  "Get the L1 coefficient for the given parameter."
  [& {:keys [layer param-name]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :param-name (:or (_ :guard string?)
                            (_ :guard seq?))}]
         `(.getL1ByParam ~layer ~param-name)
         :else
         (.getL1ByParam layer param-name)))

(defn get-l2-by-param-layer
  "Get the L2 coefficient for the given parameter."
  [& {:keys [layer param-name]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :param-name (:or (_ :guard string?)
                            (_ :guard seq?))}]
         `(.getL2ByParam ~layer ~param-name)
         :else
         (.getL2ByParam layer param-name)))

(defn get-learning-rate-by-param-layer
  "Get the (initial) learning rate coefficient for the given parameter."
  [& {:keys [layer param-name]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :param-name (:or (_ :guard string?)
                            (_ :guard seq?))}]
         `(.getLearningRateByParam ~layer ~param-name)
         :else
         (.getLearningRateByParam layer param-name)))

(defn get-output-type
  "returns the output type of the provided layer given the layer input type

  :layer-idx (int), the index of the layer within its model

  :input-type (map), the input to the cnn layer
   - {:convolutional {:height 1 :width 1 :depth 1}}
   - {:recurrent {:size 10}}
  - only 2 examples, see dl4clj.nn.conf.constants"
  [& {:keys [layer layer-idx input-type]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :layer-idx (:or (_ :guard number?)
                           (_ :guard seq?))
           :input-type (:or (_ :guard map?)
                            (_ :guard seq?))}]
         `(.getOutputType ~layer (int ~layer-idx) (enum/input-types ~input-type))
         :else
         (.getOutputType layer layer-idx (enum/input-types input-type))))

(defn get-pre-processor-for-input-type
  "For the given type of input to this layer, what preprocessor (if any) is required?

  :input-type (map), the input to the cnn layer
   - {:convolutional {:height 1 :width 1 :depth 1}}
   - {:recurrent {:size 10}}
  - only 2 examples, see dl4clj.nn.conf.constants"
  [& {:keys [layer input-type]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :input-type (:or (_ :guard map?)
                            (_ :guard seq?))}]
         `(.getPreProcessorForInputType ~layer (enum/input-types ~input-type))
         :else
         (.getPreProcessorForInputType layer (enum/input-types input-type))))

(defn get-updater-by-param
  "Get the updater for the given parameter."
  [& {:keys [layer param-name]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :param-name (:or (_ :guard string?)
                            (_ :guard seq?))}]
         `(.getUpdaterByParam ~layer ~param-name)
         :else
         (.getUpdaterByParam layer param-name)))

(defn set-n-in!
  "Set the nIn value (number of inputs, or input depth for CNNs) based on the given input type

  :input-type (map), the input to the cnn layer
   - {:convolutional {:height 1 :width 1 :depth 1}}
   - {:recurrent {:size 10}}
  - only 2 examples, see dl4clj.nn.conf.constants

  :override? (boolean), do you want to override the current n-in?

  returns the layer"
  [& {:keys [layer input-type override?]
      :as opts}]
  (match [opts]
         [{:layer (_ :guard seq?)
           :input-type (:or (_ :guard map?)
                            (_ :guard seq?))
           :override? (:or (_ :guard boolean?)
                           (_ :guard seq?))}]
         `(doto ~layer (.setNIn (enum/input-types ~input-type) ~override?))
         :else
         (doto layer (.setNIn (enum/input-types input-type) override?))))

(defn reset-layer-default-config
  [layer]
  (match [layer]
         [(_ :guard seq?)]
         `(.resetLayerDefaultConfig ~layer)
         :else
         (.resetLayerDefaultConfig layer)))
