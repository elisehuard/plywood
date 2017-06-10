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
(def bigger-dataset (let [csv (read-csv-to-ds (io/resource "test/basic_income_dataset_dalia.csv"))]
                      (d (first csv) (rest csv))))

(deftest filter-dataset-test
  (testing "filter rows of a matrix where c is nil and a < b"
    (is (.equals (ds/row-maps (filter-dataset test-ds1 [:a :b :c] (fn [a b c]
                                                                    (and (> a c) (not (nil? b))))))
                 (ds/row-maps (ds/dataset [:a :b :c] [[2 "Right" 0]])))))
  (testing "filter rows of a matrix to see if order is respected"
    (is (.equals (ds/row-maps (filter-dataset test-ds1 [:b :a :c] (fn [b a c]
                                                                    (and (> a c) (not (nil? b))))))
                 (ds/row-maps (ds/dataset [:a :b :c] [[2 "Right" 0]]))))))

;; TODO: need equality that is independent of row order ...
(deftest join-datasets
  (testing "left join"
    (is (.equals
         (left-join test-ds1 test-ds2 [:a])
         (d [:a :b :c :d :e]
            [[1 "Left" 3 "not" 4]
             [1 "Left" 3 "why" 3]
             [2 "Right" 0 nil nil]
             [4 nil 5 nil nil]]))))
  )
