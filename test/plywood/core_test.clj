(ns plywood.core-test
  (:require [clojure.test :refer :all]
            [clojure.core.matrix.dataset :as ds]
            [plywood.core :refer :all]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn read-csv-to-ds
  [filename]
  (with-open [reader (io/reader filename)]
    (doall
     (csv/read-csv reader))))

(defn d
  [ks values]
  (ds/dataset
   (map zipmap
        (repeat ks)
        values)))

(def test-ds1 (d [:a :b :c] [[1 "Left" 3] [2 "Right" 0] [4 nil 5]]))
(def test-ds2 (d [:a :d :e] [[1 "why" 3] [1 "not" 4]]))
(def test-ds3 (d [:a :d :e] [[2 "why" 3] [3 "not" 4]]))
(def bigger-dataset (let [csv (read-csv-to-ds (io/resource "test/basic_income_dataset_dalia.csv"))]
                      (d (first csv) (rest csv))))

(defn ds-equals
  "dataset equality for tests - probably very inefficient"
  [^clojure.core.matrix.impl.dataset.DataSet ds1 ^clojure.core.matrix.impl.dataset.DataSet ds2]
  (let [ds2-ordered-by-columns (ds/select-columns ds2 (ds/column-names ds1))]
    (= (.columns ds1) (.columns ds2-ordered-by-columns))))

(deftest filter-dataset-test
  (testing "filter rows of a matrix where c is nil and a < b"
    (is (= (ds/row-maps (filter-dataset test-ds1 [:a :b :c] (fn [a b c]
                                                              (and (> a c) (not (nil? b))))))
           (ds/row-maps (ds/dataset [:a :b :c] [[2 "Right" 0]])))))
  (testing "filter rows of a matrix to see if order is respected"
    (is (= (ds/row-maps (filter-dataset test-ds1 [:b :a :c] (fn [b a c]
                                                              (and (> a c) (not (nil? b))))))
           (ds/row-maps (ds/dataset [:a :b :c] [[2 "Right" 0]]))))))

;; TODO: need equality that is independent of row order or column order ...
(deftest join-datasets
  (testing "left join"
    (is (ds-equals
         (left-join test-ds1 test-ds2 [:a])
         (d [:a :b :c :d :e]
            [[1 "Left" 3 "not" 4]
             [1 "Left" 3 "why" 3]
             [2 "Right" 0 nil nil]
             [4 nil 5 nil nil]]))))
  (testing "right join"
    (is (ds-equals
         (right-join test-ds1 test-ds2 [:a])
         (d [:a :b :c :d :e]
            [[1 "Left" 3 "why" 3]
             [1 "Left" 3 "not" 4]]))))
  (testing "inner join"
    (is (ds-equals
         (inner-join test-ds1 test-ds3 [:a])
         (d [:a :b :c :d :e]
            [[2 "Right" 0 "why" 3]])))))
