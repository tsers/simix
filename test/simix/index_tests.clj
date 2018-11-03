(ns simix.index-tests
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [simix.core :as sim])
  (:import (org.apache.commons.io FileUtils)))

(deftest basic-ops
  (let [idx (sim/create :L2 5 100)]
    (sim/add! idx {:id 1 :val [0.1 0.3 0.8 0.3 0.6]})
    (sim/add! idx {:id 2 :val [0.4 0.6 0.4 0.4 0.4]})
    (sim/add! idx {:id 3 :val [0.9 0.4 0.7 0.8 0.2]})
    (is (= [3 2] (map :id (sim/q idx [0.8 0.3 0.6 0.4 0.1] 2))))
    (let [f (io/file "target/smoke_idx")]
      (if (.exists f)
        (FileUtils/forceDelete f))
      (sim/save! idx f)))
  (let [idx (sim/load (io/file "target/smoke_idx") 200)]
    (is (= [3 2] (map :id (sim/q idx [0.8 0.3 0.6 0.4 0.1] 2))))))
