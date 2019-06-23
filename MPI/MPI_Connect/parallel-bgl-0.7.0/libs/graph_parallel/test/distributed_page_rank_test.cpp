#include <boost/graph/use_mpi.hpp>
#include <boost/throw_exception.hpp>
#include <boost/graph/distributed/page_rank.hpp>
#include <boost/test/minimal.hpp>
#include <boost/graph/distributed/adjacency_list.hpp>
#include <boost/graph/distributed/mpi_process_group.hpp>
#include <boost/test/minimal.hpp>
#include <vector>
#include <iostream>
#include <stdlib.h>
#include <cstdlib>
#include <iomanip>

#include <boost/config.hpp>
#include <boost/graph/distributed/compressed_sparse_row_graph.hpp>
#include <boost/property_map/parallel/distributed_property_map.hpp>
#include <boost/graph/distributed/concepts.hpp>
#include <boost/graph/rmat_graph_generator.hpp>
#include <boost/random/linear_congruential.hpp>
#include <boost/graph/graphviz.hpp>
#include <boost/property_map/vector_property_map.hpp>

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


bool close_to(double x, double y)
{
  double diff = x - y;
  if (diff < 0) diff = -diff;
  double base = (y == 0? x : y);
  if (base != 0) return diff / base < 0.01;
  else return true;
}

// Make convenient labels for the vertices
//enum vertex_id_t { A, B, C, D, N };



void test_distributed_page_rank(char* graphfilename ,int num_ofVertex, int num_ofEdges, int iterations)
{
  //using namespace boost::graph;

  int N = num_ofVertex;
  mpi_process_group pg;
  std::size_t id = process_id(pg);
  typedef parallel::variant_distribution<mpi_process_group> Distribution;
  Distribution distrib = parallel::block(pg, N);


  // create a typedef for the Graph type
  typedef adjacency_list<vecS, 
                         distributedS<mpi_process_group, vecS>,
                         bidirectionalS 
                         > Graph;
  typedef graph_traits<Graph>::vertex_descriptor vertex_descriptor;

  // writing out the edges in the graph
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
      printf("INFO: Starting PageRank.\n");
      printf("INFO: Params: InputGraphfile=%s, num_ofVertex=%d, num_ofEdges=%d, iterations=%d\n", graphfilename , num_ofVertex, num_ofEdges, iterations );
  }
  std::vector<double> ranks(num_vertices(g));
  time_type start = get_time();
  page_rank(g, make_iterator_property_map(ranks.begin(), get(boost::vertex_index, g)),
          graph::n_iterations(iterations), 0.85, N); 
  time_type end = get_time();

  if (process_id(g.process_group()) == 0) {
      std::cout << "INFO: PageRank Complete. time = " <<  
          print_time(end - start) << "s." << std::endl;
      //printf("RESULT: %d %d %d %f %f %f %f %f\n", 
        //      num_processes(pg), n, m, a, b, c, d, end - start);
  }

  double local_sum = 0.0;
  for(unsigned int i = 0; i < num_vertices(g); ++i) {
    std::cout << g.distribution().global(i)<<" = "<< ranks[i]<< "\n";
    local_sum += ranks[i];
  }

  double sum=0.;
  boost::mpi::reduce(communicator(g.process_group()),
                     local_sum, sum, std::plus<double>(), 0);
  if (process_id(g.process_group()) == 0) {
      
      std::cout << "Sum = " << sum << "\n\n";
      //BOOST_CHECK(close_to(sum, 5000 )); // 1 when alpha=0
  }

/*
  //   double expected_ranks0[N] = {0.400009, 0.199993, 0.399998, 0.0};
  double expected_ranks[N];// = {1.49011, 0.783296, 1.5766, 0.15};
  std::memset(expected_ranks, 1/N, N*sizeof(double));
  for (int i = 0; i < N; ++i) {
    vertex_descriptor v = vertex(i, g);
    if (v != Graph::null_vertex()
        && owner(v) == process_id(g.process_group())) {
      BOOST_CHECK(close_to(ranks[local(v)], expected_ranks[i]));

    }
  }
  */
}

int test_main(int argc, char* argv[])
{
  mpi::environment env(argc, argv);

  /*
  char* inputGraph = argv[1];
  int num_ofEdges = 131655;
  int num_ofVertex = 32768;
  int iterations = 50;
  if (argc > 1) {
    iterations = atoi(argv[1]);
  }
  */
  
  if (argc < 5){
      //printf("INFO: Params: InputGraphfile=%s, num_ofVertex=%d, num_ofEdges=%d, iterations=%d\n", "amazon_gen_15.txt",32768 , 131655, 50);
      test_distributed_page_rank("amazon_gen_15.txt",32768, 131655, 50);
  }
  else{
     // printf("INFO: Params: InputGraphfile=%s, num_ofVertex=%d, num_ofEdges=%d, iterations=%d\n", argv[1], atoi(argv[2]), atoi(argv[3]), atoi(argv[4]) );
      test_distributed_page_rank(argv[1], atoi(argv[2]), atoi(argv[3]), atoi(argv[4]) );
  }

  //test_distributed_page_rank(num_ofVertex,num_ofEdges,iterations);

  return 0;
}
