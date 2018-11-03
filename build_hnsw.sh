#!/usr/bin/env bash
set -eou pipefail

HNSW_VERSION=19390a9ca14d3d140defe6e7e71825a92f768baa

if [ ! -d hnsw ]; then
  git clone https://github.com/nmslib/hnsw.git
fi

cd hnsw && git fetch && git reset --hard $HNSW_VERSION && cd ..

mkdir -p resources

CXX=g++
CXX_FLAGS="-O3 -DNDEBUG -std=c++11 -fopenmp -fPIC -march=native -shared -static-libstdc++"

if [[ "$OSTYPE" == "darwin"* ]]; then
  CXX=g++-8
  CXX_FLAGS="$CXX_FLAGS -mmacosx-version-min=10.6"
  OUTPUT=libhnsw_osx.dylib
else
  OUTPUT=libhnsw_linux.so
fi

$CXX -I$(pwd)/hnsw/hnswlib $CXX_FLAGS -o resources/$OUTPUT libhnsw.cpp
