(ns forest.geography-test
  (:require [forest.geography :refer :all]
            [clojure.test :refer :all]))

(deftest test-scale-heights-down
  (testing "scaled data"
    (is (= (scale-heights-down [1 64 6400]) [1/64 1 100]))))


(deftest test-load-png-height-map
  (testing "Check whether png height maps get loaded correctly"
    (is (= (load-png-height-map "test/forest/test-map.png")
           [[1/64, 6400/64, 640/64, 100/64],
            [1000/64, 2000/64, 3000/64, 4000/64],
            [9876/64, 8765/64, 7654/64, 6543/64],
            [1913/64, 4334/64, 3455/64, 3435/64]]))))
