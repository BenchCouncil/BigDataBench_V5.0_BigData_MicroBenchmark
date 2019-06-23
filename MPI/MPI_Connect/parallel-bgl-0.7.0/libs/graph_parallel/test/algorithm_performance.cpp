// Copyright 2004 The Trustees of Indiana University.

// Use, modification and distribution is subject to the Boost Software
// License, Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

//  Authors: Nick Edmonds
//           Andrew Lumsdaine

// #define PBGL_ACCOUNTING

#include <boost/graph/use_mpi.hpp>

#include <boost/graph/distributed/compressed_sparse_row_graph.hpp>
#include <boost/graph/distributed/adjacency_list.hpp>

#include <boost/graph/distributed/mpi_process_group.hpp>

#include <boost/test/minimal.hpp>
#include <boost/random.hpp>
#include <boost/property_map/parallel/distributed_property_map.hpp>
#include <boost/graph/distributed/graphviz.hpp>
#include <boost/graph/iteration_macros.hpp>
#include <boost/graph/properties.hpp>

#include <boost/graph/rmat_graph_generator.hpp>
#include <boost/graph/small_world_generator.hpp>
#include <boost/graph/erdos_renyi_generator.hpp>
#include <boost/graph/recursive_rmat_generator.hpp>
#include <boost/graph/permute_graph.hpp>

#include <boost/graph/distributed/connected_components.hpp>
#include <boost/graph/distributed/connected_components_parallel_search.hpp>
#include <boost/graph/distributed/betweenness_centrality.hpp>
#include <boost/graph/distributed/delta_stepping_shortest_paths.hpp>
#include <boost/graph/distributed/st_connected.hpp>
#include <boost/graph/distributed/breadth_first_search.hpp>

#include <time.h>
#include <sys/time.h>

#include <iostream>
#include <iomanip>
#include <vector>
#include <string>
#include <stdint.h>

// Edge distribution tags select a generator
struct rmat_edge_distribution_tag { };

using namespace boost;
using boost::graph::distributed::mpi_process_group;

/****************************************************************************
 * Timing
 ****************************************************************************/
#ifndef PBGL_ACCOUNTING 

typedef double time_type;

inline time_type get_time()
{
  timeval tp;
  gettimeofday(&tp, 0);
  return tp.tv_sec + tp.tv_usec / 1000000.0;
}

std::string print_time(time_type t)
{
  std::ostringstream out;
  out << std::setiosflags(std::ios::fixed) << std::setprecision(2) << t;
  return out.str();
}

#endif // PBGL_ACCOUNTING

namespace boost { namespace graph { namespace distributed {

template <typename ProcessGroup, typename OwnerMap, typename Buffer,
	  typename UnaryPredicate>
class timing_queue : 
    public boost::graph::distributed::distributed_queue<
      ProcessGroup, OwnerMap, Buffer, UnaryPredicate>
{
  typedef boost::graph::distributed::distributed_queue<
    ProcessGroup, OwnerMap, Buffer, UnaryPredicate> inherited;

public:
  timing_queue(const ProcessGroup& p, const OwnerMap& o, 
	       const UnaryPredicate& pred)
    : inherited(p, o, pred)
  { times.push_back(get_time()); }

  bool do_synchronize() {
    bool b = inherited::do_synchronize();
    times.push_back(get_time());
    return b;
  }

  std::vector<time_type> times;
};

} } } // boost::graph::distributed

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

/****************************************************************************
 * Edge Property                                                            *
 ****************************************************************************/
typedef int weight_type;

struct WeightedEdge {
  WeightedEdge(weight_type weight = 0) : weight(weight) { }
  
  weight_type weight;

  template<typename Archiver>
  void serialize(Archiver& ar, const unsigned int /*version*/)
  {
    ar & weight;
  }
};

/****************************************************************************
 * BFS depth-labeling visitor                                               *
 ****************************************************************************/

template <typename DistanceMap, typename Graph>
class bfs_depth_labeling_visitor : public bfs_visitor<>
{
  typedef typename boost::graph_traits<Graph>::vertex_descriptor Vertex;
  typedef typename boost::graph_traits<Graph>::edge_descriptor Edge;

public:

  bfs_depth_labeling_visitor(DistanceMap d)
    : distance(d) { }

  void tree_edge(const Edge& e, const Graph& g) const {
    put(distance, target(e, g), get(distance, source(e, g)) + 1);
  }

private:

  DistanceMap distance;
};

/****************************************************************************
 * Algorithm Tests                                                          *
 ****************************************************************************/
template <typename Graph>
void test_directed_sequential_algorithms(const Graph& g)
{ }

template <typename Graph>
void test_undirected_sequential_algorithms(const Graph& g)
{ 
  std::vector<unsigned int> componentS(num_vertices(g));
  typedef iterator_property_map<
      std::vector<unsigned int>::iterator, 
      typename property_map<Graph, vertex_index_t>::type> 
    ComponentMap;
  ComponentMap component(componentS.begin(), get(vertex_index, g));

  time_type start = get_time();
  unsigned int num_components = connected_components(g, component);
  time_type end = get_time();
  
  std::cerr << "    Sequential connected Components time = " 
            << print_time(end - start) << " seconds.\n" 
            << "    " << num_components << " components identified\n";
}

template <typename Graph, typename EdgeWeightMap>
void test_directed_csr_only_algorithms(const Graph& g, EdgeWeightMap weight,
                                       typename graph_traits<Graph>::vertices_size_type num_sources,
                                       typename property_traits<EdgeWeightMap>::value_type C,
				       std::vector<std::string> algorithms_to_run)
{
  typedef typename graph_traits<Graph>::vertex_descriptor vertex_descriptor;
  typedef typename graph_traits<Graph>::vertex_iterator vertex_iterator;
  typedef typename graph_traits<Graph>::vertices_size_type vertices_size_type;
  typedef typename graph_traits<Graph>::edges_size_type edges_size_type;

  typedef typename boost::graph::parallel::process_group_type<Graph>::type 
    process_group_type;

  process_group_type pg = process_group(g);
  typename process_group_type::process_id_type id = process_id(pg);
  typename process_group_type::process_size_type p = num_processes(pg);    

}

template <typename Graph, typename EdgeWeightMap>
void test_directed_algorithms(const Graph& g, EdgeWeightMap weight,
			      typename graph_traits<Graph>::vertices_size_type num_sources,
			      typename property_traits<EdgeWeightMap>::value_type C,
			      std::vector<std::string> algorithms_to_run)
{
  typedef typename graph_traits<Graph>::vertex_descriptor vertex_descriptor;
  typedef typename graph_traits<Graph>::vertex_iterator vertex_iterator;
  typedef typename graph_traits<Graph>::vertices_size_type vertices_size_type;
  typedef typename graph_traits<Graph>::edges_size_type edges_size_type;

  typedef typename boost::graph::parallel::process_group_type<Graph>::type 
    process_group_type;

  process_group_type pg = process_group(g);
  typename process_group_type::process_id_type id = process_id(pg);
  typename process_group_type::process_size_type p = num_processes(pg);    

  vertices_size_type n = num_vertices(g);
  n = boost::parallel::all_reduce(pg, n, std::plus<vertices_size_type>());

  edges_size_type m = num_edges(g);
  m = boost::parallel::all_reduce(pg, m, std::plus<edges_size_type>());

  typedef typename property_map<Graph, vertex_owner_t>::const_type OwnerMap;
  typedef typename property_map<Graph, vertex_local_t>::const_type LocalMap;
  OwnerMap owner = get(vertex_owner, g);
  LocalMap local = get(vertex_local, g);

  //
  // Betweenness Centrality (Approximate)
  //
  queue<vertex_descriptor> delta_stepping_vertices;

  if (std::find(algorithms_to_run.begin(), algorithms_to_run.end(), "delta-stepping")
      != algorithms_to_run.end() ||
      std::find(algorithms_to_run.begin(), algorithms_to_run.end(), "betweenness-centrality")
      != algorithms_to_run.end() ||
      std::find(algorithms_to_run.begin(), algorithms_to_run.end(), "bfs")
      != algorithms_to_run.end()) { 

    // Calculate number of vertices of degree 0
    vertices_size_type n0 = 0;
    BGL_FORALL_VERTICES_T(v, g, Graph) {
      if (out_degree(v, g) == 0) n0++;
    }
    n0 = boost::parallel::all_reduce(pg, n0, std::plus<vertices_size_type>());

    queue<vertex_descriptor> sources;

    {
      minstd_rand gen;
      uniform_int<vertices_size_type> rand_vertex(0, num_vertices(g) - 1);
      std::vector<vertex_descriptor> all_sources, local_sources;
      vertices_size_type local_vertices = vertices_size_type(floor((double)num_sources / p));
      local_vertices += (id < (num_sources - (p * local_vertices)) ? 1 : 0); 

      // Make sure every process has enough sources
      vertices_size_type num_non_zeros;
      BGL_FORALL_VERTICES_T(v, g, Graph) {
	if (out_degree(v, g) > 0)
	  ++num_non_zeros;
      }
      assert(num_non_zeros > local_vertices);

      while (local_vertices > 0) {
        vertex_iterator iter = vertices(g).first;
	vertices_size_type temp = rand_vertex(gen);
        std::advance(iter, temp);
        
        if (out_degree(*iter, g) != 0
            && std::find(local_sources.begin(), local_sources.end(), *iter) == local_sources.end()) {
          local_sources.push_back(*iter);
          --local_vertices;
        }
      }
      all_gather(pg, local_sources.begin(), local_sources.end(), all_sources);
      std::sort(all_sources.begin(), all_sources.end());
      for (typename std::vector<vertex_descriptor>::iterator iter = all_sources.begin(); 
           iter != all_sources.end(); ++iter) {
        sources.push(*iter);
        delta_stepping_vertices.push(*iter);
      }
    }

    if (std::find(algorithms_to_run.begin(), algorithms_to_run.end(), "betweenness-centrality")
	!= algorithms_to_run.end()) {

      // Distributed Centrality Map
      typedef typename property_map<Graph, vertex_index_t>::const_type IndexMap;
      typedef iterator_property_map<std::vector<int>::iterator, IndexMap> CentralityMap;
    
      std::vector<int> centralityS(num_vertices(g), 0);
      CentralityMap centrality(centralityS.begin(), get(vertex_index, g));

      // NOTE: The delta below assumes uniform edge weight distribution
      time_type start = get_time();
      brandes_betweenness_centrality(g, buffer(sources).weight_map(weight).
				     centrality_map(centrality).lookahead((m / n) * (C / 2)));
      time_type end = get_time();
      
      edges_size_type exactTEPs = edges_size_type(floor(7 * n* (n - n0) / (end - start)));
      
      if (id == 0)
	std::cerr << "    Betweenness Centrality Approximate (" << num_sources << " sources) = " 
		  << print_time(end - start) << " (" << exactTEPs << " TEPs)\n";
    }

    if (std::find(algorithms_to_run.begin(), algorithms_to_run.end(), "bfs")
	!= algorithms_to_run.end()) {

#define STATS

#ifdef STATS
      typedef typename property_map<Graph, vertex_index_t>::const_type IndexMap;
      typedef iterator_property_map<std::vector<int>::iterator, IndexMap> DistanceMap;
      
      std::vector<int> distanceS(num_vertices(g), 0);
      DistanceMap distance(distanceS.begin(), get(vertex_index, g));

      set_property_map_role(vertex_distance, distance);

      std::vector<size_t> all_distances;
      std::vector<time_type> all_times;

      // Timing queue initialization
      typedef two_bit_color_map<IndexMap> ColorMap;
      ColorMap color(num_vertices(g), get(vertex_index, g));

      typedef boost::graph::distributed::timing_queue<
        process_group_type, OwnerMap, boost::queue<vertex_descriptor>,
	detail::darken_and_push<ColorMap> > queue_t;

      queue_t Q(pg, owner, detail::darken_and_push<ColorMap>(color));
#endif

      time_type start = get_time();
      while (!sources.empty()) {
#ifdef STATS
	distance.clear();
	BGL_FORALL_VERTICES_T(v, g, Graph) {
	  put(distance, v, std::numeric_limits<int>::max());
	}
	put(distance, sources.top(), 0); 
#endif

#ifdef STATS
	breadth_first_search(g, sources.top(), Q, 
			     bfs_depth_labeling_visitor<DistanceMap, Graph>(distance), color);
#else
	breadth_first_search(g, sources.top(), 
 			     visitor(bfs_depth_labeling_visitor<DistanceMap, Graph>(distance)));
#endif

#ifdef STATS
	BGL_FORALL_VERTICES_T(v, g, Graph) {
	  int dist = get(distance, v);
	  if (dist != std::numeric_limits<int>::max()) {
	    if (all_distances.size() < dist + 1) all_distances.resize(dist + 1);
	    ++all_distances[dist];
	  }
	}

	if (Q.times.size() > all_times.size()) 
	  all_times.resize(Q.times.size(), time_type(0));

	for(int i = 1 ; i < Q.times.size() ; ++i)
	  all_times[i] += Q.times[i] - Q.times[i-1];
#endif
	sources.pop();
      }
      time_type end = get_time();

#ifdef STATS
      if (id == 0)
	std::cerr << "Distances:\n";

      int size = 
 	boost::parallel::all_reduce(pg, all_distances.size(), boost::parallel::maximum<int>());
      all_distances.resize(size, 0);

      if (id == 0)
	std::cerr << std::setw(5) << "Phase" << " " << std::setw(8) << "Vertices" << " " 
		  << std::setw(6) << "Time\n";

      for (int i = 0 ; i < all_distances.size() ; ++i) {
	all_distances[i] = 
	  boost::parallel::all_reduce(pg, all_distances[i], std::plus<int>());
	if (id == 0)
	  std::cerr << std::setw(5) << i << std::setw(8) << all_distances[i] << std::setw(6) 
		    << print_time(all_times[i+1] / num_sources) << std::endl;
      }
#endif

      if (id == 0)
	std::cerr << "    Breadth-First Search (" << num_sources << " sources) = " 
		  << print_time(end - start) << std::endl;
    }
  }

  //
  // Delta stepping shortest paths
  //
  if (std::find(algorithms_to_run.begin(), algorithms_to_run.end(), "delta-stepping")
      != algorithms_to_run.end()) {
    typedef typename property_map<Graph, vertex_index_t>::const_type IndexMap;
    typedef iterator_property_map<std::vector<int>::iterator, IndexMap> DistanceMap;
    
    std::vector<int> distanceS(num_vertices(g), 0);
    DistanceMap distance(distanceS.begin(), get(vertex_index, g));

    time_type total_time = 0;
    while(!delta_stepping_vertices.empty()) {

      time_type start = get_time();
      delta_stepping_shortest_paths(g, delta_stepping_vertices.top(), dummy_property_map(),
                                    distance, weight);
      time_type end = get_time();
  
      delta_stepping_vertices.pop();
      distance.reset();
      total_time += end - start;
    }

    if (id == 0)
      std::cerr << "    Delta-stepping shortest paths (average over " << num_sources 
		<< " sources) = " << print_time(total_time) << std::endl;
  }
}

template <typename Graph>
void test_undirected_algorithms(const Graph& g, size_t num_sources, 
				std::vector<std::string> algorithms_to_run)
{
  typedef typename graph_traits<Graph>::vertex_descriptor vertex_descriptor;
  typedef typename graph_traits<Graph>::vertices_size_type vertices_size_type;

  typedef typename boost::graph::parallel::process_group_type<Graph>::type 
    process_group_type;

  process_group_type pg = process_group(g);
  typename process_group_type::process_id_type id = process_id(pg);
  typename process_group_type::process_size_type p = num_processes(pg);    

  vertices_size_type n = num_vertices(g);
  n = boost::parallel::all_reduce(pg, n, std::plus<vertices_size_type>());

  typedef typename property_map<Graph, vertex_owner_t>::const_type OwnerMap;
  typedef typename property_map<Graph, vertex_local_t>::const_type LocalMap;
  OwnerMap owner = get(vertex_owner, g);
  LocalMap local = get(vertex_local, g);

  //
  // Connected Components
  //
  if (std::find(algorithms_to_run.begin(), algorithms_to_run.end(), "connected-components")
      != algorithms_to_run.end()) {

    std::vector<unsigned int> local_components_vec(num_vertices(g));
    typedef iterator_property_map<
    std::vector<unsigned int>::iterator, 
      typename property_map<Graph, vertex_index_t>::type> 
    ComponentMap;
    ComponentMap component(local_components_vec.begin(), get(vertex_index, g));

    int num_components;
    
    time_type start = get_time();
    num_components = connected_components(g, component);
    time_type end = get_time();
    if (id == 0) 
      std::cerr << "    Connected Components time = " << print_time(end - start) 
		<< " seconds.\n" 
		<< "      " << num_components << " components identified\n";
    
    start = get_time();
    num_components = boost::graph::distributed::connected_components_ps(g, component);
    end = get_time();
    if (id == 0) 
      std::cerr << "    Connected Components (parallel search) time = " 
		<< print_time(end - start) << " seconds.\n"
		<< "      " << num_components << " components identified\n";
  }

  //
  // s-t Connectivity
  //

  queue<std::pair<vertex_descriptor, vertex_descriptor> > sources;

  if (std::find(algorithms_to_run.begin(), algorithms_to_run.end(), "st-connected")
      != algorithms_to_run.end()) {

    minstd_rand gen;
    uniform_int<vertices_size_type> rand_vertex(0, n - 1);

    size_t remaining_sources = num_sources;
    while (remaining_sources > 0) {
      std::pair<vertex_descriptor, vertex_descriptor> 
	st(vertex(rand_vertex(gen), g), vertex(rand_vertex(gen), g));
	
      bool zero_degree = false;
      if (get(owner, st.first) == id && out_degree(st.first, g) == 0
	  || get(owner, st.second) == id && out_degree(st.second, g) == 0)
	zero_degree = true;

      bool invalid = 
	boost::parallel::all_reduce(pg, zero_degree, std::logical_or<bool>());

      if (!invalid && st.first != st.second) {
	sources.push(st);
	--remaining_sources;
      }
    }

    time_type total_time = 0;
    while (!sources.empty()) {
      time_type start = get_time();
      st_connected(g, sources.top().first, sources.top().second);
      time_type end = get_time();

      total_time += end - start;
    }

    if (id == 0) 
      std::cerr << "    st-Connectivity time (average over " << num_sources
		<< "source-target pairs) = " << print_time(total_time) 
		<< " seconds.\n";
  }
}

/****************************************************************************
 * Graph Type Tests                                                         *
 ****************************************************************************/

// TODO: Re-seed PRNG before sequential tests to get the same graph as the 
// distributed case?

//
// Compressed Sparse Row
//

// R-MAT 
template <typename ProcessGroup, typename RandomGenerator, typename Distribution>
void test_csr(const ProcessGroup& pg, RandomGenerator& gen, Distribution& distrib,
              bool sequential_tests, size_t N, size_t M, size_t C, 
              double a, double b, double c, double d, size_t num_sources,
              std::vector<std::string> algorithms_to_run, rmat_edge_distribution_tag)
{
  if (process_id(pg) == 0)
    std::cerr << "  R-MAT\n";

  std::vector<std::pair<size_t, size_t> > edges;

#if 0
  {
    typedef compressed_sparse_row_graph<directedS, no_property, WeightedEdge, no_property,
      distributedS<mpi_process_group> > DiGraph;

    typedef typename graph_traits<DiGraph>::vertices_size_type vertices_size_type;
    typedef recursive_rmat_generator<ProcessGroup, RandomGenerator, DiGraph> RMATGen;

    // Permutation
    {
      RMATGen begin(pg, gen, static_cast<vertices_size_type>(floor(log2(N))), 
		    M, a, b, c, d), 
	end;

      std::vector<std::pair<size_t, size_t> > in_edges; 
      while (begin != end) { in_edges.push_back(*begin); ++begin; }

      // Permute and redistribute copy constructs the input iterator
      permute_and_redistribute_simple<false, DiGraph>
 	(N, in_edges.begin(), in_edges.end(), gen, distrib, 
	 std::back_inserter(edges), communicator(pg));
    }

    // In-place CSR ctor is unnecessary as we needed 2E storage to do the permutation
    // now in_edges has been freed and we can re-use it's memory
    
    DiGraph g(edges_are_unsorted_multi_pass, edges.begin(), edges.end(),
	      make_generator_iterator(gen, uniform_int<int>(1, C)),
	      N, pg, distrib);
  
    test_directed_algorithms(g, get(&WeightedEdge::weight, g), num_sources, C,
			     algorithms_to_run);
    test_directed_csr_only_algorithms(g, get(&WeightedEdge::weight, g), num_sources, C,
				      algorithms_to_run);
  }
#endif

  if (sequential_tests && process_id(pg
) == 0) {
    typedef compressed_sparse_row_graph<directedS, no_property, WeightedEdge>
      seqGraph;
    
    seqGraph sg(edges_are_unsorted_multi_pass, edges.begin(), edges.end(),
                make_generator_iterator(gen, uniform_int<int>(1, C)),
                N);
    
    test_directed_sequential_algorithms(sg);
  }
}

// Erdos-Renyi
template <typename ProcessGroup, typename RandomGenerator, typename Distribution>
void test_csr(const ProcessGroup& pg, RandomGenerator& gen, Distribution& distrib,
              bool sequential_tests, size_t N, size_t M, size_t C, size_t num_sources, 
	      std::vector<std::string> algorithms_to_run)
{
  if (process_id(pg) == 0)
    std::cerr << "  Erdos-Renyi\n";

  // 2 divides are more stable numerically than a single divide by N^2
  double _p = static_cast<double>(M) / N / N;

  typedef compressed_sparse_row_graph<directedS, no_property, WeightedEdge, no_property,
                                      distributedS<mpi_process_group> > Graph;
    
  Graph g(edges_are_sorted,
	  sorted_erdos_renyi_iterator<RandomGenerator, Graph>(gen, N, _p/2),
          sorted_erdos_renyi_iterator<RandomGenerator, Graph>(),
          make_generator_iterator(gen, uniform_int<int>(1, C)),
          N, pg, distrib);
  
  test_directed_algorithms(g, get(&WeightedEdge::weight, g), num_sources, C,
			   algorithms_to_run);
  test_directed_csr_only_algorithms(g, get(&WeightedEdge::weight, g), num_sources, C,
				    algorithms_to_run);

  if (sequential_tests && process_id(pg) == 0) {
    typedef compressed_sparse_row_graph<directedS, no_property, WeightedEdge>
      seqGraph;
    
    seqGraph sg(edges_are_sorted,
		sorted_erdos_renyi_iterator<RandomGenerator, seqGraph>(gen, N, _p/2),
                sorted_erdos_renyi_iterator<RandomGenerator, seqGraph>(),
                make_generator_iterator(gen, uniform_int<int>(1, C)),
                N);
    
    test_directed_sequential_algorithms(sg);
  }
}

// Small World
template <typename ProcessGroup, typename RandomGenerator, typename Distribution>
void test_csr(const ProcessGroup& pg, RandomGenerator& gen, Distribution& distrib,
              bool sequential_tests, size_t N, size_t M, size_t C, double p, 
              size_t num_sources, std::vector<std::string> algorithms_to_run)
{
  if (process_id(pg) == 0)
    std::cerr << "  Small-World\n";

  int k = M / N;

  typedef compressed_sparse_row_graph<directedS, no_property, WeightedEdge, no_property,
                                      distributedS<mpi_process_group> > Graph;

  Graph g(edges_are_sorted,
	  small_world_iterator<RandomGenerator, Graph>(gen, N, k, p),
          small_world_iterator<RandomGenerator, Graph>(),
          make_generator_iterator(gen, uniform_int<int>(1, C)),
          N, pg, distrib);
  
  test_directed_algorithms(g, get(&WeightedEdge::weight, g), num_sources, C,
			   algorithms_to_run);
  test_directed_csr_only_algorithms(g, get(&WeightedEdge::weight, g), num_sources, C,
				    algorithms_to_run);

  if (sequential_tests && process_id(pg) == 0) {
    typedef compressed_sparse_row_graph<directedS, no_property, WeightedEdge>
      seqGraph;
    
    seqGraph sg(edges_are_sorted,
		small_world_iterator<RandomGenerator, seqGraph>(gen, N, k, p),
                small_world_iterator<RandomGenerator, seqGraph>(),
                make_generator_iterator(gen, uniform_int<int>(1, C)),
                N);
    
    test_directed_sequential_algorithms(sg);
  }
}

//
// Adjacency List
//

// R-MAT 
template <typename ProcessGroup, typename RandomGenerator, typename Distribution>
void test_adjacency_list(const ProcessGroup& pg, RandomGenerator& gen, Distribution& distrib,
                         bool sequential_tests, size_t N, size_t M, size_t C, 
                         double a, double b, double c, double d, size_t num_sources,
                         std::vector<std::string> algorithms_to_run, rmat_edge_distribution_tag)
{
  if (process_id(pg) == 0)
    std::cerr << "  R-MAT\n";

  std::vector<std::pair<size_t, size_t> > edges; 

  {
    typedef adjacency_list<vecS, distributedS<mpi_process_group, vecS>,
                           directedS, no_property, WeightedEdge> DiGraph;

    typedef typename graph_traits<DiGraph>::vertices_size_type vertices_size_type;

    typedef recursive_rmat_generator<ProcessGroup, RandomGenerator, DiGraph> RMATGen;

    // Permutation
    {
      RMATGen begin(pg, gen, static_cast<vertices_size_type>(floor(log2(N))), 
		    M, a, b, c, d), 
	end;

      std::vector<std::pair<size_t, size_t> > in_edges; 
      while (begin != end) { in_edges.push_back(*begin); ++begin; }

      // Permute and redistribute copy constructs the input iterator
      permute_and_redistribute_simple<false, DiGraph>
 	(N, in_edges.begin(), in_edges.end(), gen, distrib, 
	 std::back_inserter(edges), communicator(pg));
    }

    DiGraph g(edges.begin(), edges.end(), 
	      make_generator_iterator(gen, uniform_int<int>(1, C)),
	      N, pg, distrib);

    test_directed_algorithms(g, get(&WeightedEdge::weight, g), num_sources, C, algorithms_to_run);
  }

  {
    typedef adjacency_list<vecS, distributedS<mpi_process_group, vecS>,
                           undirectedS, no_property, WeightedEdge> Graph;
    
    Graph g(edges.begin(), edges.end(),
            make_generator_iterator(gen, uniform_int<int>(1, C)),
            N, pg, distrib);

    test_undirected_algorithms(g, num_sources, algorithms_to_run);
  }

  if (sequential_tests && process_id(pg) == 0) {
    {
      typedef adjacency_list<vecS, vecS, directedS, no_property, property<edge_weight_t, int> > 
        seqGraph;
      
      seqGraph sg(edges.begin(), edges.end(),
                  make_generator_iterator(gen, uniform_int<int>(1, C)),
                  N);

      test_directed_sequential_algorithms(sg);
    }

    {
      typedef adjacency_list<vecS, vecS, undirectedS, no_property, property<edge_weight_t, int> > 
        seqGraph;
      
      seqGraph sg(edges.begin(), edges.end(),
                  make_generator_iterator(gen, uniform_int<int>(1, C)),
                  N);

      test_undirected_sequential_algorithms(sg);
    }
  }
}

// Erdos-Renyi
template <typename ProcessGroup, typename RandomGenerator, typename Distribution>
void test_adjacency_list(const ProcessGroup& pg, RandomGenerator& gen, Distribution& distrib,
                         bool sequential_tests, size_t N, size_t M, size_t C, 
			 size_t num_sources, std::vector<std::string> algorithms_to_run)
{
  if (process_id(pg) == 0)
    std::cerr << "  Erdos-Renyi\n";

  // 2 divides are more stable numerically than a single divide by N^2
  double _p = static_cast<double>(M) / N / N; 

  {
    typedef adjacency_list<vecS, distributedS<mpi_process_group, vecS>,
                           directedS, no_property, WeightedEdge> Graph;

    Graph g(erdos_renyi_iterator<RandomGenerator, Graph>(gen, N, _p/2),
            erdos_renyi_iterator<RandomGenerator, Graph>(),
            make_generator_iterator(gen, uniform_int<int>(1, C)),
            N, pg, distrib);

    test_directed_algorithms(g, get(&WeightedEdge::weight, g), num_sources, C, algorithms_to_run);
  }

  {
    typedef adjacency_list<vecS, distributedS<mpi_process_group, vecS>,
                           undirectedS, no_property, WeightedEdge> Graph;

    Graph g(erdos_renyi_iterator<RandomGenerator, Graph>(gen, N, _p/2),
            erdos_renyi_iterator<RandomGenerator, Graph>(),
            make_generator_iterator(gen, uniform_int<int>(1, C)),
            N, pg, distrib);
    
    test_undirected_algorithms(g, num_sources, algorithms_to_run);
  }

  if (sequential_tests && process_id(pg) == 0) {
    {
      typedef adjacency_list<vecS, vecS, directedS, no_property, property<edge_weight_t, int> > 
        seqGraph;

      seqGraph sg(erdos_renyi_iterator<RandomGenerator, seqGraph>(gen, N, _p/2),
                  erdos_renyi_iterator<RandomGenerator, seqGraph>(),
                  make_generator_iterator(gen, uniform_int<int>(1, C)),
                  N);
      
      test_directed_sequential_algorithms(sg);
    }

    {
      typedef adjacency_list<vecS, vecS, undirectedS, no_property, property<edge_weight_t, int> > 
        seqGraph;

      seqGraph sg(erdos_renyi_iterator<RandomGenerator, seqGraph>(gen, N, _p/2),
                  erdos_renyi_iterator<RandomGenerator, seqGraph>(),
                  make_generator_iterator(gen, uniform_int<int>(1, C)),
                  N);
      
      test_undirected_sequential_algorithms(sg);
    }
  }
}

// Small World
template <typename ProcessGroup, typename RandomGenerator, typename Distribution>
void test_adjacency_list(const ProcessGroup& pg, RandomGenerator& gen, Distribution& distrib,
                         bool sequential_tests, size_t N, size_t M, size_t C, double p,
			 size_t num_sources, std::vector<std::string> algorithms_to_run)
{
  if (process_id(pg) == 0)
    std::cerr << "  Small-World\n";

  int k = M / N;

  {
    typedef adjacency_list<vecS, distributedS<mpi_process_group, vecS>,
                           directedS, no_property, WeightedEdge> Graph;

    Graph g(small_world_iterator<RandomGenerator, Graph>(gen, N, k, p),
            small_world_iterator<RandomGenerator, Graph>(),
            make_generator_iterator(gen, uniform_int<int>(1, C)),
            N, pg, distrib);

    test_directed_algorithms(g, get(&WeightedEdge::weight, g), num_sources, C, algorithms_to_run);
  }

  {
    typedef adjacency_list<vecS, distributedS<mpi_process_group, vecS>,
                           undirectedS, no_property, WeightedEdge> Graph;

    Graph g(small_world_iterator<RandomGenerator, Graph>(gen, N, k, p),
            small_world_iterator<RandomGenerator, Graph>(),
            make_generator_iterator(gen, uniform_int<int>(1, C)),
            N, pg, distrib);

    test_undirected_algorithms(g, num_sources, algorithms_to_run);
  }

  if (sequential_tests && process_id(pg) == 0) {
    {
      typedef adjacency_list<vecS, vecS, directedS, no_property, property<edge_weight_t, int> > 
        seqGraph;
      
      seqGraph sg(small_world_iterator<RandomGenerator, seqGraph>(gen, N, k, p),
                  small_world_iterator<RandomGenerator, seqGraph>(),
                  make_generator_iterator(gen, uniform_int<int>(1, C)),
                  N);
      
      test_directed_sequential_algorithms(sg);
    }

    {
      typedef adjacency_list<vecS, vecS, undirectedS, no_property, property<edge_weight_t, int> > 
        seqGraph;
      
      seqGraph sg(small_world_iterator<RandomGenerator, seqGraph>(gen, N, k, p),
                  small_world_iterator<RandomGenerator, seqGraph>(),
                  make_generator_iterator(gen, uniform_int<int>(1, C)),
                  N);
      
      test_undirected_sequential_algorithms(sg);
    }
  }
}

void usage()
{
  std::cerr << "Algorithm Performance Test\n"
            << "Usage : algorithm_performance [options]\n\n"
            << "Options are:\n"
            << "\t--vertices v\t\t\tNumber of vertices in the graph\n"
            << "\t--edges v\t\t\tNumber of edges in the graph\n"
            << "\t--seed s\t\t\tSeed for synchronized random number generator\n"
            << "\t--max-weight miw\t\tMaximum integer edge weight\n"
            << "\t--rewire-probability\t\tRewire-probabitility (p) for small-world graphs\n"
            << "\t--dot\t\t\t\tEmit a dot file containing the graph\n"
            << "\t--verify\t\t\tVerify result\n"
            << "\t--degree-dist\t\t\tOutput degree distribution of graph\n"
            << "\t--sequential-graph\t\tRun sequential graph tests\n"
            << "\t--erdos-renyi\t\t\tRun tests on Erdos-Renyi graphs\n"   
            << "\t--small-world\t\t\tRun tests on Small World graphs\n"   
            << "\t--rmat\t\t\t\tRun tests on R-MAT graphs (with unique edges)\n"   
            << "\t--csr <bool>\t\t\tRun tests using CSR graphs\n"   
            << "\t--adjacency-list <bool>\t\tRun tests using Adjacency List graphs\n"   
            << "\t--rmat_a <double>\t\t'a' parameter to R-MAT distribution\n"   
            << "\t--rmat_b <double>\t\t'b' parameter to R-MAT distribution\n"   
            << "\t--rmat_c <double>\t\t'c' parameter to R-MAT distribution\n"   
            << "\t--rmat_d <double>\t\t'd' parameter to R-MAT distribution\n"   
	    << "\t--p <double>\t\t\tpair-wise edge probability for Erdos-Renyi graphs\n";
}


int test_main(int argc, char* argv[])
{
  mpi::environment env(argc, argv);

  rand48 gen;
  boost::graph::random::splittable_ecuyer1988 splittable_gen;

  // Default args
  size_t n = 100, 
    m = 8*n, 
    c = 100,
    num_sources = 32,
    num_headers = 16 * 1024,
    batch_buffer_size = 1024 * 1024;
  uint64_t seed = 1 + (boost::lexical_cast<uint64_t>(1) << 33);
  bool emit_dot_file = false, 
    verify = false,
    sequential_graph = false,
    show_degree_dist = false,
    erdos_renyi = false,
    small_world = false,
    rmat = false,
    csr = false,
    adj_list = false;
  double p = 0.1,
    rmat_a = 0.57, 
    rmat_b = 0.19,
    rmat_c = 0.19,
    rmat_d = 0.05; 
  std::vector<std::string> algorithms_to_run;

  // Parse args
  for (int i = 1; i < argc; ++i) {
    std::string arg = argv[i];

    if (arg == "--vertices")
      n = boost::lexical_cast<size_t>( argv[i+1] );

    if (arg == "--seed")
      seed = boost::lexical_cast<uint64_t>( argv[i+1] );

    if (arg == "--edges")
      m = boost::lexical_cast<size_t>( argv[i+1] );

    if (arg == "--max-weight")
      c = boost::lexical_cast<int>( argv[i+1] );

    if (arg == "--batch-buffer-size") {
      batch_buffer_size = boost::lexical_cast<size_t>( argv[i+1] );
      num_headers = batch_buffer_size / 16;
      num_headers = num_headers > 0 ? num_headers : 1;
    }

    if (arg == "--rewire-probability")
      p = boost::lexical_cast<double>( argv[i+1] );

    if (arg == "--num-sources")
      num_sources = boost::lexical_cast<size_t>( argv[i + 1] );

    if (arg == "--dot")
      emit_dot_file = true;

    if (arg == "--verify")
      verify = true;

    if (arg == "--degree-dist")
      show_degree_dist = true;

    if (arg == "--sequential-graph")
      sequential_graph = true;
    
    // Graph Data Structures
    if (arg == "--csr")
      csr = true;

    if (arg == "--adjacency-list")
      adj_list = true;

    // Graph Generators
    if (arg == "--erdos-renyi")
      erdos_renyi = true;

    if (arg == "--small-world")
      small_world = true;

    if (arg == "--rmat")
      rmat = true;

    if (arg == "--rmat_a")
      rmat_a = boost::lexical_cast<double>( argv[i + 1] );

    if (arg == "--rmat_b")
      rmat_b = boost::lexical_cast<double>( argv[i + 1] );

    if (arg == "--rmat_c")
      rmat_c = boost::lexical_cast<double>( argv[i + 1] );

    if (arg == "--rmat_d")
      rmat_d = boost::lexical_cast<double>( argv[i + 1] );

    if (arg == "--p")
      p = boost::lexical_cast<double>( argv[i + 1] ); 

    // Algorithms
    if (arg == "--bfs")
      algorithms_to_run.push_back("bfs");

    if (arg == "--betweenness-centrality" || arg == "--bc")
      algorithms_to_run.push_back("betweenness-centrality");

    if (arg == "--delta-stepping")
      algorithms_to_run.push_back("delta-stepping");

    if (arg == "--connected-components" || arg == "--cc")
      algorithms_to_run.push_back("connected-components");

    if (arg == "--connected-components-parallel-search" || arg == "--cc-ps")
      algorithms_to_run.push_back("connected-components-parallel-search");

    if (arg == "--st-connected" || arg == "--st")
      algorithms_to_run.push_back("st-connected");
  }

  mpi_process_group pg(num_headers, batch_buffer_size);

  if (argc == 1) {
    if (process_id(pg) == 0)
      usage();
    exit(-1);
  }

  if (rmat)
    assert(seed >> 32 != 0 && seed << 32 != 0);
  else
    assert(seed != 0);
      
  gen.seed(seed);
  splittable_gen.seed(seed >> 32, seed << 32);

  parallel::variant_distribution<mpi_process_group> distrib 
    = parallel::block(pg, n);

  if (csr) {
    if (process_id(pg) == 0)
      std::cerr << "CSR Graph Tests\n";

    if (erdos_renyi)
      test_csr(pg, gen, distrib, sequential_graph, n, m, c, num_sources, algorithms_to_run);
    if (small_world)
      test_csr(pg, gen, distrib, sequential_graph, n, m, c, p, num_sources, algorithms_to_run);
    if (rmat)
      test_csr(pg, splittable_gen, distrib, sequential_graph, n, m, c, rmat_a, rmat_b, rmat_c, 
	       rmat_d, num_sources, algorithms_to_run, rmat_edge_distribution_tag());
  }

  gen.seed(seed); // DEBUG 

  if (adj_list) {
    if (process_id(pg) == 0)
      std::cerr << "Adjacency List Graph Tests\n";

    if (erdos_renyi)
      test_adjacency_list(pg, gen, distrib, sequential_graph, n, m, c, num_sources, 
			  algorithms_to_run);
    if (small_world)
      test_adjacency_list(pg, gen, distrib, sequential_graph, n, m, c, p, num_sources,
			  algorithms_to_run);
    if (rmat)
      test_adjacency_list(pg, splittable_gen, distrib, sequential_graph, n, m, c, rmat_a, 
			  rmat_b, rmat_c, rmat_d, num_sources, algorithms_to_run, 
			  rmat_edge_distribution_tag());
  }

  return 0;
}
