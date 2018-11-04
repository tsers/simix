#include <iostream>
#include <string>
#include <limits>
#include <sstream>
#include "hnswlib.h"

struct Index {
  int d;
  hnswlib::HierarchicalNSW<float> *hnsw;
  hnswlib::SpaceInterface<float> *space;
  Index(int d, hnswlib::SpaceInterface<float>* s, hnswlib::HierarchicalNSW<float>* h) {
    this->d = d;
    this->space = s;
    this->hnsw = h;
  }
};

static hnswlib::SpaceInterface<float>* _hnsw_create_space(int space_type, int dim) {
  switch(space_type) {
    case 1: return new hnswlib::L2Space(dim);
    case 2: return new hnswlib::InnerProductSpace(dim);
    default: return nullptr;
  }
}

extern "C" {

int hnsw_get_max_id_value(char* output) {
  std::stringstream buffer;
  buffer << std::numeric_limits<std::size_t>::max();
  strcpy(output, buffer.str().c_str());
  return buffer.str().length();
}

Index* hnsw_create_index(int space_type, int dim, int max_elems, int M, int ef_construction, int random_seed) {
  auto space = _hnsw_create_space(space_type, dim);
  if (!space) {
    return nullptr;
  }
  auto hnsw = new hnswlib::HierarchicalNSW<float>(space, max_elems, M, ef_construction, random_seed);
  return new Index(dim, space, hnsw);
}

Index*  hnsw_load_index(const char* index_path, int space_type, int dim, int max_elems) {
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

void hnsw_set_query_ef(Index* index, int ef) {
  index->hnsw->ef_ = ef;
}

void hnsw_add_item(Index* index, long id, float* data) {
  index->hnsw->addPoint((void *) data, (size_t) id);
}

void hnsw_knn_query(Index* index, float* x, int k, float* distances, long* ids) {
  auto results = index->hnsw->searchKnn(x, k);
  size_t i = 0;
  while (!results.empty()) {
    const std::pair<float, size_t >& res = results.top();
    distances[i] = res.first;
    ids[i] = (long) res.second;
    results.pop();
    ++i;
  }
  for (; i < k; i++) {
    ids[i] = -1L;
  }
}

}