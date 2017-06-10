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

(defn column-values-fn
  [dataset columns]
  (let [col-indexes (map (partial ds/column-index dataset) columns)]
    (fn [row]
      (map (partial nth row) col-indexes))))

(defn build-index [indexer inverse-indexer dataset]
  (->> dataset
       cm/rows
       (r/fold
        (fn combiner
          ([] {})
          ([l r]
           (merge l r)))
        (fn reducer
          ([] {})
          ([a row]
           (update a
                   (indexer row)
                   #(conj % (inverse-indexer row))))))))

(defn join-
  "Right joins the two datasets by the values found in columns, where the left side of the join is target.
  Implementation assumes the cost of converting the left dataset to rows and then using fold to join right, will be
  justified by the size of the data. Potential improve could be to detect the data size and if small perform the join by
  creating new columns for the dataset, rather than growing the rows."
  [target src [t-columns s-columns] {:keys [empty-cell inner] :or {:empty-cell nil :inner false}}]
  (let [src-indexer (column-values-fn src s-columns)
        unindexed-cols (remove (set s-columns) (ds/column-names src))
        inverse-indexer (column-values-fn src unindexed-cols)
        dex (build-index src-indexer inverse-indexer src)
        t-indexer (column-values-fn target t-columns)
        unmatched-index (repeat (count unindexed-cols) empty-cell)]
    (->> target
         cm/rows
         (r/fold
          (fn combiner
            ([] [])
            ([l r]
             (concat l r)))
          (fn reducer
            ([] [])
            ([a row]
             (let [matched-index (get dex (t-indexer row))]
               (if matched-index
                 (concat a (mapv #(concat row %) matched-index))
                 (if (not inner)
                   (concat a [(concat row unmatched-index)])
                   a))))))
         (ds/dataset (concat (ds/column-names target)
                             unindexed-cols)))))

(defn left-join
  [left right columns & options]
  (join- left right
         (if (vector? (first columns)) columns (repeat 2 columns))
         (apply hash-map options)))

(defn right-join
  [left right columns & options]
  (join- right left
         (if (vector? (first columns)) columns (repeat 2 columns))
         (apply hash-map options)))

(defn full-join
  [left right columns & options])
