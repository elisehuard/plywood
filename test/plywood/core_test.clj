(ns plywood.core-test
  (:require [clojure.test :refer :all]
            [clojure.core.matrix.dataset :as ds]
            [plywood.core :refer :all]))

(def test-ds1 (ds/dataset [:a :b :c] [[1 1 "Left"] [3 2 "Right"] [4 5 nil]]))

(deftest filter-dataset-test
  (testing "filter rows of a matrix where c is nil and a < b"
    (is (.equals (ds/row-maps (filter-dataset test-ds1 [:a :b :c] (fn [a b c]
                                                                    (and (> a b) (not (nil? c))))))
                 (ds/row-maps (ds/dataset [:a :b :c] [[3 2 "Right"]]))))))
