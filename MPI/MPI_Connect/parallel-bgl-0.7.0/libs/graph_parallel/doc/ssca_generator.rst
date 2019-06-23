.. Copyright (C) 2004-2009 The Trustees of Indiana University.
   Use, modification and distribution is subject to the Boost Software
   License, Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
   http://www.boost.org/LICENSE_1_0.txt)

===========================
|Logo| SSCA Generator
===========================

::

  template<typename RandomGenerator, typename Graph>
  class ssca_iterator
  {
  public:
    typedef std::input_iterator_tag iterator_category;
    typedef std::pair<vertices_size_type, vertices_size_type> value_type;
    typedef const value_type& reference;
    typedef const value_type* pointer;
    typedef void difference_type;

    ssca_iterator();
    ssca_iterator(RandomGenerator& gen, vertices_size_type totVertices, 
		  vertices_size_type maxCliqueSize, double probUnidirectional, 
		  int maxParallelEdges, double probIntercliqueEdges);
    // Iterator operations
    reference operator*() const;
    pointer operator->() const;
    ssca_iterator& operator++();
    ssca_iterator operator++(int);
    bool operator==(const ssca_iterator& other) const;
    bool operator!=(const ssca_iterator& other) const;
  };

This class template implements the generator from the Scalable
Synthetic Compact Application #2 Graph Analysis version 1.1 benchmark
[KKD05]_.

Where Defined
-------------
<``boost/graph/ssca_graph_generator.hpp``>

Constructors
------------

::

  ssca_iterator();

Constructs a past-the-end iterator.

::

  ssca_iterator(RandomGenerator& gen, vertices_size_type totVertices, 
                vertices_size_type maxCliqueSize, double probUnidirectional, 
                int maxParallelEdges, double probIntercliqueEdges);

Constructs an SSCA generator iterator that creates a graph with
``totVertices`` vertices.  ``maxCliqueSize`` represents the maximum
size of generated cliques.  ``probUnidirectional`` represents the
probability that a link between two vertices will be uni-directional.
``maxParallelEdges`` represents the maximum number of parallel edges
between a pair of vertices. ``probIntercliqueEdges`` represents the
probability that an edge exists connecting two cliques. Probabilities
are drawn from the random number generator gen.

Example
-------

::

  #include <boost/graph/adjacency_list.hpp>
  #include <boost/graph/ssca_graph_generator.hpp>
  #include <boost/random/linear_congruential.hpp>

  typedef boost::adjacency_list<> Graph;
  typedef boost::ssca_iterator<boost::minstd_rand, Graph> SSCAGen;

  int main()
  {
    boost::minstd_rand gen;
    // Create graph with 100 nodes and 400 edges 
    Graph g(SSCAGen(gen, 100, 10, 0.5, 5, 0.1), SSCAGen(), 100);
    return 0;
  }


Bibliography
------------

.. [KKD05] Kepner, J., Koester, D. P., et al. HPCS SSCA #2 Graph
  Analysis Benchmark Specifications v1.0, April 2005.

-----------------------------------------------------------------------------

Copyright (C) 2009 The Trustees of Indiana University.

Authors: Nick Edmonds and Andrew Lumsdaine

.. |Logo| image:: http://www.osl.iu.edu/research/pbgl/images/pbgl-logo.png
            :align: middle
            :alt: Parallel BGL
            :target: http://www.osl.iu.edu/research/pbgl
