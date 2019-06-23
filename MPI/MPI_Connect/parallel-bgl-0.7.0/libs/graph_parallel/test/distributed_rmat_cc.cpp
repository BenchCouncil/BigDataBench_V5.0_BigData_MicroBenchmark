#include <boost/graph/use_mpi.hpp>
#include <boost/config.hpp>
#include <boost/throw_exception.hpp>
#include <boost/graph/distributed/adjacency_list.hpp>
#include <boost/property_map/parallel/distributed_property_map.hpp>
#include <boost/graph/distributed/mpi_process_group.hpp>
#include <boost/graph/distributed/concepts.hpp>
#include <boost/graph/distributed/connected_components_parallel_search.hpp>
#include <boost/graph/distributed/connected_components.hpp>
#include <boost/graph/rmat_graph_generator.hpp>
#include <boost/random/linear_congruential.hpp>
#include <boost/graph/graphviz.hpp>
#include <boost/property_map/vector_property_map.hpp>

#include <iostream>
#include <cstdlib>
#include <iomanip>

#ifdef BOOST_NO_EXCEPTIONS
void
boost::throw_exception(std::exception const& ex)
{
  std::cout << ex.what() << std::endl;
  abort();
}
#endif

using namespace boost;
using boost::graph::distributed::mpi_process_group;

typedef double time_type;

inline time_type get_time()
{
  return MPI_Wtime();
}

std::string print_time(time_type t)
{
  std::ostringstream out;
  out << std::setiosflags(std::ios::fixed) << std::setprecision(2) << t;
  return out.str();
}

void
test_filtered_rmat_cc(char* graphfilename ,int num_ofVertex, int num_ofEdges)
{

  int N = num_ofVertex;
  mpi_process_group pg;
  mpi_process_group::process_id_type id = process_id(pg);

  //if (id == 0) printf("INFO: Params: n=%d, m=%d, a=%f, b=%f, c=%f, d=%f.\n", n, m, a, b, c, d);

  parallel::variant_distribution<mpi_process_group> distrib 
    = parallel::block(pg, N);

  typedef adjacency_list<vecS, 
        distributedS<mpi_process_group, vecS>,
        undirectedS> Graph;

  typedef keep_local_edges<parallel::variant_distribution<mpi_process_group>,
                           mpi_process_group::process_id_type>
    EdgeFilter; 

  //typedef unique_rmat_iterator<rand48, Graph, EdgeFilter>
    //RMATIter;

  //if (id == 0) printf("INFO: Generating graph.\n");

  //rand48 gen;
  //Graph g(RMATIter(gen, n, m, a, b, c, d, true, EdgeFilter(distrib, id)), 
    //      RMATIter(), n, pg, distrib);

typedef std::pair<int, int> Edge;
  //init Graph
  //std::ifstream graphfile("./amazon_gen_15.txt");
  std::ifstream graphfile(graphfilename);
  std::string line;
  //Edge edge_array[] = { Edge(A,B), Edge(A,C), Edge(B,C), Edge(C,A), Edge(D,C) };
  Edge edge_array[num_ofEdges];
  memset(edge_array,0,sizeof(int)); 
  //

  //Graph g;
  //std::map<int,int> edge_array;
  int i=0;

  //std::cout<<"!!!!!"<<std::endl;
  while(getline(graphfile,line)!=NULL){
      
      std::string::size_type index = line.find_first_of("\t",0);
      std::string::size_type end = line.find_first_of("\n",0);
      std::string inedge_s = line.substr(0,index);
      int inedge_num = std::atoi(inedge_s.c_str());
      std::string outedge_s = line.substr(index+1,end);
      int outedge_num = std::atoi(outedge_s.c_str());

      //add_edge(inedge_num, outedge_num, g);
      //edge_array.insert(inedge_num,outedge_num);

      edge_array[i].first=inedge_num;
      edge_array[i].second=outedge_num;
      //std::cout << edge_array[i].first <<" "<<edge_array[i].second<< std::endl;
      i++;
  }
  const int num_edges = sizeof(edge_array)/sizeof(edge_array[0]);

  // declare a graph object
  Graph g(edge_array, edge_array + num_edges, N);
  
  
  //std::vector<double> ranks(num_vertices(g));

  //page_rank(g,
  //          make_iterator_property_map(ranks.begin(),
   //                                    get(boost::vertex_index, g)),
   //         n_iterations(iterations), 0.85, N);
    
  synchronize(g);
  if (id == 0) {
      printf("INFO: Starting connected components.\n");
      printf("INFO: Params: InputGraphfile=%s, num_ofVertex=%d, num_ofEdges=%d\n", graphfilename , num_ofVertex, num_ofEdges );
  }


  std::vector<int> local_components_vec(num_vertices(g));
  typedef iterator_property_map<std::vector<int>::iterator, property_map<Graph, vertex_index_t>::type> ComponentMap;
  ComponentMap component(local_components_vec.begin(), get(vertex_index, g));

  time_type start = get_time();
  int num_components = connected_components(g, component);
  time_type end = get_time();

  if (process_id(g.process_group()) == 0) {
      std::cout << "INFO: Test Complete. components found = " << num_components
                << ", time = " << print_time(end - start) << "s." << std::endl;
  }

}

int
main(int argc, char* argv[])
{
  mpi::environment env(argc, argv);

  if (argc < 5){
      //printf("INFO: Params: InputGraphfile=%s, num_ofVertex=%d, num_ofEdges=%d, iterations=%d\n", "amazon_gen_15.txt",32768 , 131655, 50);
      test_filtered_rmat_cc("amazon_gen_15.txt",32768, 131655);
  }
  else{
     // printf("INFO: Params: InputGraphfile=%s, num_ofVertex=%d, num_ofEdges=%d, iterations=%d\n", argv[1], atoi(argv[2]), atoi(argv[3]), atoi(argv[4]) );
      test_filtered_rmat_cc(argv[1], atoi(argv[2]), atoi(argv[3]));
  }

  //test_distributed_page_rank(num_ofVertex,num_ofEdges,iterations);

  return 0;
}
