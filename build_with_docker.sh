#!/usr/bin/env bash
set -euo pipefail

docker run --rm -ti \
  -v $(cd $(dirname $0) && pwd):/build \
  -w /build \
  gcc:8 \
  /build/build_hnsw.sh