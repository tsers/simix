(ns simix.core
  (:refer-clojure :exclude [load])
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import (simix LibHNSW HNSWWrapper SimixException)
           (com.sun.jna Memory)
           (java.io File Closeable)))

(defmacro -use-hnsw [[b v] & body]
  `(let [f# (fn [p#] (let [~b p#] (do ~@body)))
         h# ~v]
     (.use h# f#)))

(defn- c-arr [sz x]
  (doto (Memory. (* sz (count x)))
    (.write 0 x 0 (count x))))

(defn- c-floats [x]
  (c-arr 4 (float-array x)))

(defn- c-longs [x]
  (c-arr 8 (long-array x)))

(defn- mem->floats [m]
  (let [arr (float-array (/ (.size m) 4))]
    (.read m 0 arr 0 (count arr))
    (seq arr)))


(defn- mem->longs [m]
  (let [arr (long-array (/ (.size m) 8))]
    (.read m 0 arr 0 (count arr))
    (seq arr)))

(defprotocol Index
  (dim [_])
  (add! [_ item])
  (q [_ x k])
  (set-ef! [_ ef])
  (save! [_ path]))

(defrecord HNSWIndex [wrapper d space-type max-items counter]
  Index
  (dim [_] d)
  (add! [_ {:keys [id val]}]
    {:pre [(sequential? val)
           (= d (count val))
           (pos? id)]}
    (-use-hnsw [ptr wrapper]
      (swap! counter #(if (> (inc %) max-items)
                        (throw (SimixException. (str "Max item count " max-items " exceeded") nil))
                        (inc %)))
      (LibHNSW/hnsw_add_item ptr (long id) (c-floats val))))
  (q [_ x k]
    {:pre [(sequential? x)
           (= d (count x))
           (pos? k)]}
    (-use-hnsw [ptr wrapper]
      (let [dists (c-floats (map (constantly (float 0)) (range k)))
            ids   (c-longs (map (constantly (long 0)) (range k)))]
        (LibHNSW/hnsw_knn_query ptr (c-floats x) (int k) dists ids)
        (->> (map vector (mem->longs ids) (mem->floats dists))
             (take-while (comp not neg? first))
             (reverse)
             (mapv (fn [[id d]] {:id (long id) :distance d}))))))
  (set-ef! [_ ef]
    {:pre [(pos? ef)]}
    (-use-hnsw [ptr wrapper] (LibHNSW/hnsw_set_query_ef ptr (int ef))))
  (save! [_ index-path]
    {:pre [(instance? File index-path)]}
    (-use-hnsw [ptr wrapper]
      (if (.exists index-path)
        (throw (SimixException. "Index directory already exists" nil)))
      (.mkdirs index-path)
      (letfn [(save! [count]
                (LibHNSW/hnsw_save_index ptr (.getAbsolutePath (io/file index-path "data")))
                (->> {:space-type space-type
                      :dimension  d
                      :num-items  count}
                     (pr-str)
                     (spit (io/file index-path "meta.edn")))
                count)]
        (swap! counter save!)
        nil)))
  Closeable
  (close [_] (.close wrapper)))

(defn- create-hnsw [ptr d space-type max-items n]
  (->HNSWIndex (HNSWWrapper. ptr) d space-type max-items (atom n)))

(defn create
  ([space-type d max-items
    {:keys [M ef-construct seed]
     :or   {M            32
            ef-construct 200
            seed         777}}]
   {:pre [(pos? d)
          (contains? #{:L2 :cosine} space-type)
          (pos? max-items)
          (pos? M)
          (pos? ef-construct)
          (pos? seed)]}
   (let [t   (case space-type
               :L2 1
               :cosine 2)
         ptr (LibHNSW/hnsw_create_index (int t) (int d) (int max-items) (int M) (int ef-construct) (int seed))]
     (create-hnsw ptr d space-type max-items 0)))
  ([space-type d max-items]
   (create space-type d max-items {})))

(defn load [index-path max-items]
  {:pre [(instance? File index-path)
         (or (nil? max-items)
             (pos? max-items))]}
  (let [data (io/file index-path "data")
        meta (io/file index-path "meta.edn")]
    (if-not (and (.exists data) (.exists meta))
      (throw (SimixException. "Required index files not found" nil)))
    (let [{:keys [space-type dimension num-items]} (edn/read-string (slurp meta))
          _   (if (and (some? max-items) (< max-items num-items))
                (throw (SimixException. (format "Index contains more items (%d) than given max-items (%d)" num-items max-items) nil)))
          max (or max-items num-items)
          t   (case space-type
                :L2 1
                :cosine 2)
          ptr (LibHNSW/hnsw_load_index (.getAbsolutePath data) t dimension max)]
      (create-hnsw ptr dimension space-type max num-items))))

(defn max-id-value []
  (let [output (Memory. 100)]
    (LibHNSW/hnsw_get_max_id_value output)
    (max (BigInteger. (.getString output 0 "ASCII")) Long/MAX_VALUE)))