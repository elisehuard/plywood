(ns plywood.core
  (:require [clojure.core.matrix.dataset :as ds]
            [clojure.core.matrix.impl.dataset :as dsi]
            [clojure.core.matrix :as cm]
            [clojure.core.reducers :as r]))

(defn filter-dataset
  "filter rows of a core matrix dataset based on a predicate function"
  [dataset filter-columns filter-fn]
  (->> dataset
       (ds/row-maps)
       (r/filter
        #(apply filter-fn (vals (select-keys % filter-columns))))
       (into [])
       (ds/dataset filter-columns)))
