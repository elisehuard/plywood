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

(def test-ds1 (ds/dataset [:a :b :c] [[1 1 "Left"] [3 2 "Right"] [4 5 nil]]))
(def test-ds2 (let [csv (read-csv-to-ds (io/resource "test/basic_income_dataset_dalia.csv"))]
                (ds/dataset (first csv) (rest csv))))

(deftest filter-dataset-test
  (testing "filter rows of a matrix where c is nil and a < b"
    (is (.equals (ds/row-maps (filter-dataset test-ds1 [:a :b :c] (fn [a b c]
                                                                    (and (> a b) (not (nil? c))))))
                 (ds/row-maps (ds/dataset [:a :b :c] [[3 2 "Right"]])))))
  (testing "filter rows of a matrix to see if order is respected"
    (is (.equals (ds/row-maps (filter-dataset test-ds1 [:b :a :c] (fn [b a c]
                                                                    (and (> a b) (not (nil? c))))))
                 (ds/row-maps (ds/dataset [:a :b :c] [[3 2 "Right"]])))))
  (testing "filter rows of a matrix to see if order is respected"
    (is (.equals (ds/row-maps (filter-dataset test-ds1 [:a :b] (fn [a b] (> a b))))
                 (ds/row-maps (ds/dataset [:a :b :c] [[3 2 "Right"]]))))))

(deftest filter-dataset2-test
  (testing "filter rows of a matrix where c is nil and a < b"
    (is (.equals (ds/row-maps (filter-dataset2 test-ds1 [:a :b :c] (fn [a b c]
                                                                     (and (> a b) (not (nil? c))))))
                 (ds/row-maps (ds/dataset [:a :b :c] [[3 2 "Right"]]))))))
