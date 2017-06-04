(ns plywood.core
  (:require [clojure.core.matrix.dataset :as ds]
            [clojure.core.matrix.impl.dataset :as dsi]
            [clojure.core.matrix :as cm]
            [clojure.core.reducers :as r]
            [clojure.core.matrix.protocols :as mp]))

(defn filter-dataset
  "Filters the given dataset to rows where filter-fn returns truthy when called with the values in filter-columns"
  [^clojure.core.matrix.impl.dataset.DataSet dataset filter-columns filter-fn]
  (let [filter-indexes (map #(ds/column-index dataset %) filter-columns)
        filter-on (fn [column-values]
                    (map #(get column-values %) filter-indexes))
        empty-data (mapv (constantly []) (ds/column-names dataset))]
    (->> (.columns dataset)
         (apply (partial mapv vector))
         (r/fold
          (fn combiner
            ([] empty-data)
            ([l r]
             (mapv concat l r)))
          (fn reducer
            ([] empty-data)
            ([a column-values]
             (if (apply filter-fn (filter-on column-values))
               (mapv #(conj %1 %2) a column-values)
               a))))
         (zipmap (ds/column-names dataset))
         ds/dataset)))
