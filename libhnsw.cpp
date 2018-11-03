#include <iostream>
#include <string>
#include "hnswlib.h"

struct Index {
  size_t d;
  hnswlib::HierarchicalNSW<float> *hnsw;
  hnswlib::SpaceInterface<float> *space;
  Index(size_t d, hnswlib::SpaceInterface<float>* s, hnswlib::HierarchicalNSW<float>* h) {
    this->d = d;
    this->space = s;
    this->hnsw = h;
  }
};

static hnswlib::SpaceInterface<float>* _hnsw_create_space(int space_type, size_t dim) {
  switch(space_type) {
    case 1: return new hnswlib::L2Space(dim);
    case 2: return new hnswlib::InnerProductSpace(dim);
    default: return nullptr;
  }
}

extern "C" {

Index* hnsw_create_index(int space_type, size_t dim, size_t max_elems, size_t M, size_t ef_construction, size_t random_seed) {
  auto space = _hnsw_create_space(space_type, dim);
  if (!space) {
    return nullptr;
  }
  auto hnsw = new hnswlib::HierarchicalNSW<float>(space, max_elems, M, ef_construction, random_seed);
  return new Index(dim, space, hnsw);
}

Index*  hnsw_load_index(const char* index_path, int space_type, size_t dim, size_t max_elems) {
  auto space = _hnsw_create_space(space_type, dim);
  if (!space) {
    return nullptr;
  }
  auto hnsw = new hnswlib::HierarchicalNSW<float>(space, std::string(index_path), false, max_elems);
  return new Index(dim, space, hnsw);
}

void hnsw_save_index(Index* index, const char* index_path) {
  index->hnsw->saveIndex(std::string(index_path));
}

void hnsw_release_index(Index* index) {
  delete index->space;
  delete index->hnsw;
  delete index;
}

void hnsw_set_query_ef(Index* index, size_t ef) {
  index->hnsw->ef_ = ef;
}

void hnsw_add_item(Index* index, int id, float* data) {
  index->hnsw->addPoint((void *) data, id);
}

void hnsw_knn_query(Index* index, float* x, size_t k, float* distances, int* ids) {
  auto results = index->hnsw->searchKnn(x, k);
  size_t i = 0;
  while (!results.empty()) {
    const std::pair<float, size_t >& res = results.top();
    distances[i] = res.first;
    ids[i] = (int) res.second;
    results.pop();
    ++i;
  }
  for (; i < k; i++) {
    ids[i] = -1;
  }
}

}