# simix

Similarity index for Clojure, built on top of [hnsw](https://github.com/nmslib/hnsw).

[![Build Status](https://img.shields.io/travis/tsers/simix/master.svg?style=flat-square)](https://travis-ci.org/tsers/simix)
[![Clojars Project](https://img.shields.io/clojars/v/simix.svg?style=flat-square)](https://clojars.org/simix)

## Usage

### APIl

#### `(create space-type d max-items {:keys [M ef-construct seed] :as opts})`

Creates a index with the given space type (`:L2` or `:cosine`), dimension and
maximum number of items. Index can be configured with the following (optional)
options (see [this](https://github.com/nmslib/hnsw/blob/master/ALGO_PARAMS.md)
for more info):

- `M` - the number of bi-directional links created for every new element during index construction (default `32`)
- `ef-construct` - the size of the dynamic list for the nearest neighbors during index construction (default `200`)
- `seed` - random seed number (default: `777`)

#### `(load index-path max-items)`

Loads the index from the given `index-path` (must be an instance of `java.io.File`).
It's possible to increase the maximum of index items during the load.

#### `(save! index-path)`

Saves the index to the given `index-path` (must be an instance of `java.io.File`).

#### `(dim index)`

Returns the dimension of the given index.

#### `(add! index {:keys [id val]})`

Adds new vector to the given index. Vector must be a sequential of floats with
size matching to the dimension of the index. Vector must also have an integer id
associated to it.

#### `(q index x k)`

Performs a kNN search to the given index with the given vector `x`.

#### `(set-ef! index ef)`

Changes the query `ef` value for the index (see [this](https://github.com/nmslib/hnsw/blob/master/ALGO_PARAMS.md#search-parameters)
for an explanation).

### Basic usage

Here is a simple RELP-ready demo containing all of the basic API functions:

```clj
(ns demo
  (:require [clojure.java.io :as io]
            [simix.core :as sim]))

; create new L2 index of dim(5) and max 100 items
(def idx (sim/create :L2 5 100))

; add some data
(sim/add! idx {:id 1 :val [0.1 0.3 0.8 0.3 0.6]})
(sim/add! idx {:id 2 :val [0.4 0.6 0.4 0.4 0.4]})
(sim/add! idx {:id 3 :val [0.9 0.4 0.7 0.8 0.2]})

; kNN query, where k = 2
(sim/q idx [0.8 0.3 0.6 0.4 0.1] 2)
; => [{:id 3, :distance 0.19999999}
;     {:id 2, :distance 0.38000005}]

; save index to HD
(sim/save! idx (io/file "my_idx"))

; load the index and increase the maximum number
; of items to 200
(def loaded (sim/load (io/file "my_idx") 200)

; kNN query returns same results as the original index
(sim/q loaded [0.8 0.3 0.6 0.4 0.1] 2)
; => [{:id 3, :distance 0.19999999}
;     {:id 2, :distance 0.38000005}]
```

### Closing unused index

Every index is closed (and memory released) when JVM GC sweeps the stale reference
to the index. However, if you want to explicitly close the index and release its
resources before Java GC, you can do it with `(.close index)`.

Index implements Java's `Closeable` so it can be used in `with-open` macro as well:

```clj
(with-open [idx (sim/load "my_idx" 200)]
  ; use the loaded index ...
  )
```

## License

MIT
