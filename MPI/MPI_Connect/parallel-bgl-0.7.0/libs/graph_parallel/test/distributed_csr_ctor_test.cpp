// Copyright (C) 2009 The Trustees of Indiana University.

// Use, modification and distribution is subject to the Boost Software
// License, Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

//  Authors: Nick Edmonds
//           Andrew Lumsdaine

#define BOOST_GRAPH_USE_NEW_CSR_INTERFACE

// A test of the distributed compressed sparse row graph type
#include <boost/graph/use_mpi.hpp>
#include <boost/config.hpp>
#include <boost/graph/distributed/compressed_sparse_row_graph.hpp>
#include <boost/graph/distributed/mpi_process_group.hpp>
#include <boost/graph/erdos_renyi_generator.hpp>
#include <boost/random/linear_congruential.hpp>
#include <boost/graph/breadth_first_search.hpp>
#include <boost/test/minimal.hpp>

/****************************************************************************
 * Edge weight generator iterator                                           *
 ****************************************************************************/
template<typename F, typename RandomGenerator>
class generator_iterator
{
public:
  typedef std::input_iterator_tag iterator_category;
  typedef typename F::result_type value_type;
  typedef const value_type&       reference;
  typedef const value_type*       pointer;
  typedef void                    difference_type;

  explicit 
  generator_iterator(RandomGenerator& gen, const F& f = F()) 
    : f(f), gen(&gen) 
  { 
    value = this->f(gen); 
  }

  reference operator*() const  { return value; }
  pointer   operator->() const { return &value; }

  generator_iterator& operator++()
  {
    value = f(*gen);
    return *this;
  }

  generator_iterator operator++(int)
  {
    generator_iterator temp(*this);
    ++(*this);
    return temp;
  }

  bool operator==(const generator_iterator& other) const
  { return f == other.f; }

  bool operator!=(const generator_iterator& other) const
  { return !(*this == other); }

private:
  F f;
  RandomGenerator* gen;
  value_type value;
};

template<typename F, typename RandomGenerator>
inline generator_iterator<F, RandomGenerator> 
make_generator_iterator( RandomGenerator& gen, const F& f)
{ return generator_iterator<F, RandomGenerator>(gen, f); }

using namespace boost;
using boost::graph::distributed::mpi_process_group;

typedef int weight_type;

struct EdgeProperty {
  EdgeProperty(weight_type weight = 0) : weight(weight) { }
  
  weight_type weight;

  template<typename Archiver>
  void serialize(Archiver& ar, const unsigned int /*version*/)
  {
    ar & weight;
  }
};

struct VertexProperty {
  VertexProperty(int d = 0)
    : distance(d) { }

  int distance;

  template<typename Archiver>
  void serialize(Archiver& ar, const unsigned int /*version*/)
  {
    ar & distance;
  }
};

int test_main(int argc, char* argv[])
{
  mpi::environment env(argc, argv);

  int n = 40;
  double prob = 0.1;
  int c = 10;

  mpi_process_group pg;
//   parallel::oned_block_cyclic dist(pg, n);
  parallel::block dist(pg, n);

  minstd_rand gen;

  // No properties
  {
    typedef compressed_sparse_row_graph<directedS, no_property, no_property, 
                                        no_property, distributedS<mpi_process_group> >
      Digraph;

    typedef sorted_erdos_renyi_iterator<minstd_rand, Digraph> SortedERIter;
    typedef erdos_renyi_iterator<minstd_rand, Digraph> ERIter;

    std::vector<ERIter::value_type> edges;
    std::vector<graph_traits<Digraph>::vertex_descriptor> sources, targets;
    ERIter start(gen, n, prob);
    for ( ; start != ERIter() ; ++start) {
      edges.push_back(*start);
      
      if (dist(start->first) == process_id(pg)) { 
        sources.push_back(start->first);
        targets.push_back(start->second);
      }
    }

    Digraph g(edges_are_unsorted, ERIter(gen, n, prob), ERIter(), n, pg, dist);
    breadth_first_search(g, vertex(0, g), visitor(bfs_visitor<>()));

    Digraph g2(edges_are_sorted, SortedERIter(gen, n, prob), SortedERIter(), n, pg, dist);
    breadth_first_search(g2, vertex(0, g2), visitor(bfs_visitor<>()));

    Digraph g3(edges_are_unsorted_multi_pass, edges.begin(), edges.end(), n, pg, dist);
    breadth_first_search(g3, vertex(0, g3), visitor(bfs_visitor<>()));

    Digraph g4(distributed_construct_inplace_from_sources_and_targets, sources, targets, n, pg, dist);
    breadth_first_search(g4, vertex(0, g4), visitor(bfs_visitor<>()));

    Digraph g5(n, pg, parallel::oned_block_cyclic(pg, 1));
    breadth_first_search(g5, vertex(0, g5), visitor(bfs_visitor<>()));

    // Test structural modification
#if 0
    typedef graph_traits<Digraph>::vertex_descriptor vertex_descriptor;
    vertex_descriptor v = g.add_vertex();

    std::vector<std::pair<vertex_descriptor, vertex_descriptor> > new_edges;

    new_edges.push_back(std::make_pair(*vertices(g).first, v));
    new_edges.push_back(std::make_pair(v, *vertices(g).first));

    add_edges(new_edges.rbegin(), new_edges.rend(), g);
    add_edges_sorted(new_edges.begin(), new_edges.end(), g);
#endif
  }

  return 0;

  // Edge properties
  {
    typedef compressed_sparse_row_graph<directedS, no_property, EdgeProperty, no_property, 
                                        distributedS<mpi_process_group> >
      Digraph;

    typedef sorted_erdos_renyi_iterator<minstd_rand, Digraph> SortedERIter;
    typedef erdos_renyi_iterator<minstd_rand, Digraph> ERIter;

    std::vector<ERIter::value_type> edges;
    std::vector<graph_traits<Digraph>::vertex_descriptor> sources, targets;
    std::vector<EdgeProperty> edge_props;
    uniform_int<int> edge_weight(0, c);

    ERIter start(gen, n, prob);
    for ( ; start != ERIter() ; ++start) {
      edges.push_back(*start);
      
      if (dist(start->first) == process_id(pg)) { 
        sources.push_back(start->first);
        targets.push_back(start->second);
        edge_props.push_back(EdgeProperty(edge_weight(gen)));
      }
    }

    Digraph g(edges_are_unsorted, ERIter(gen, n, prob), ERIter(), n, pg, dist);
    breadth_first_search(g, vertex(0, g), visitor(bfs_visitor<>()));

    Digraph g2(edges_are_sorted, SortedERIter(gen, n, prob), SortedERIter(), n, pg, dist);
    breadth_first_search(g2, vertex(0, g2), visitor(bfs_visitor<>()));

    Digraph g3(edges_are_unsorted_multi_pass, edges.begin(), edges.end(), 
	       make_generator_iterator(gen, uniform_int<int>(0, c)), n, pg, dist);
    breadth_first_search(g3, vertex(0, g3), visitor(bfs_visitor<>()));

     Digraph g4(distributed_construct_inplace_from_sources_and_targets, sources, 
		targets, edge_props, n, pg, dist);
    breadth_first_search(g4, vertex(0, g4), visitor(bfs_visitor<>()));

    Digraph g5(n, pg, parallel::oned_block_cyclic(pg, 1));
    breadth_first_search(g5, vertex(0, g5), visitor(bfs_visitor<>()));

    // Test structural modification
#if 0
    typedef graph_traits<Digraph>::vertex_descriptor vertex_descriptor;
    vertex_descriptor v = add_vertex(g);

    std::vector<std::pair<vertex_descriptor, vertex_descriptor> > new_edges;
    std::vector<EdgeProperty> new_edge_props;

    new_edges.push_back(std::make_pair(*vertices(g).first, v));
    new_edges.push_back(std::make_pair(v, *vertices(g).first));

    new_edge_props.resize(2);

    add_edges(new_edges.rbegin(), new_edges.rend(), g);
    add_edges_sorted(new_edges.begin(), new_edges.end(), g);

    add_edges(new_edges.rbegin(), new_edges.rend(),
	      new_edge_props.begin(), new_edge_props.end(), g);
    add_edges_sorted(new_edges.begin(), new_edges.end(), 
		     new_edge_props.begin(), g);
#endif
  }

  exit(-1);

  // Edge and vertex properties
  {
    typedef compressed_sparse_row_graph<directedS, VertexProperty, EdgeProperty, 
                                        no_property, 
                                        distributedS<mpi_process_group> >
      Digraph;

    typedef sorted_erdos_renyi_iterator<minstd_rand, Digraph> SortedERIter;
    typedef erdos_renyi_iterator<minstd_rand, Digraph> ERIter;

    std::vector<ERIter::value_type> edges;
    std::vector<graph_traits<Digraph>::vertex_descriptor> sources, targets;
    std::vector<EdgeProperty> edge_props;
    uniform_int<int> edge_weight(0, c);

    ERIter start(gen, n, prob);
    for ( ; start != ERIter() ; ++start) {
      edges.push_back(*start);
      
      if (dist(start->first) == process_id(pg)) { 
        sources.push_back(start->first);
        targets.push_back(start->second);
        edge_props.push_back(EdgeProperty(edge_weight(gen)));
      }
    }

    Digraph g(edges_are_unsorted, ERIter(gen, n, prob), ERIter(), n, pg, dist);
    breadth_first_search(g, vertex(0, g), visitor(bfs_visitor<>()));

    Digraph g2(edges_are_sorted, SortedERIter(gen, n, prob), SortedERIter(), n, pg, dist);
    breadth_first_search(g2, vertex(0, g2), visitor(bfs_visitor<>()));

    Digraph g3(edges_are_unsorted_multi_pass, edges.begin(), edges.end(), 
	       make_generator_iterator(gen, uniform_int<int>(0, c)), n, pg, dist);
    breadth_first_search(g3, vertex(0, g3), visitor(bfs_visitor<>()));

    Digraph g4(distributed_construct_inplace_from_sources_and_targets, sources, 
               targets, edge_props, n, pg, dist);
    breadth_first_search(g4, vertex(0, g4), visitor(bfs_visitor<>()));

    Digraph g5(n, pg, parallel::oned_block_cyclic(pg, 1));
    breadth_first_search(g5, vertex(0, g5), visitor(bfs_visitor<>()));

    // Test structural modification
#if 0
    typedef graph_traits<Digraph>::vertex_descriptor vertex_descriptor;
    vertex_descriptor v = add_vertex(g);

    std::vector<std::pair<vertex_descriptor, vertex_descriptor> > new_edges;
    std::vector<EdgeProperty> new_edge_props;

    new_edges.push_back(std::make_pair(*vertices(g).first, v));
    new_edges.push_back(std::make_pair(v, *vertices(g).first));
    new_edge_props.resize(2);

    add_edges(new_edges.rbegin(), new_edges.rend(), g);
    add_edges_sorted(new_edges.begin(), new_edges.end(), g);

    add_edges(new_edges.rbegin(), new_edges.rend(),
	      new_edge_props.begin(), new_edge_props.end(), g);
		     
    add_edges_sorted(new_edges.begin(), new_edges.end(),
		     new_edge_props.begin(), g);

    add_vertex(VertexProperty(), g);
#endif
  }

  return 0;
}
