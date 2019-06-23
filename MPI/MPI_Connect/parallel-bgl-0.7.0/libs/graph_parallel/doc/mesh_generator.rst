.. Copyright (C) 2004-2009 The Trustees of Indiana University.
   Use, modification and distribution is subject to the Boost Software
   License, Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
   http://www.boost.org/LICENSE_1_0.txt)

===========================
|Logo| Mesh Generator
===========================

::

  template<typename Graph>
  class mesh_iterator
  {
  public:
    typedef std::input_iterator_tag iterator_category;
    typedef std::pair<vertices_size_type, vertices_size_type> value_type;
    typedef const value_type& reference;
    typedef const value_type* pointer;
    typedef void difference_type;

    mesh_iterator();
    mesh_iterator(vertices_size_type x, vertices_size_type y, 
		  bool toroidal = true);
    // Iterator operations
    reference operator*() const;
    pointer operator->() const;
    mesh_iterator& operator++();
    mesh_iterator operator++(int);
    bool operator==(const mesh_iterator& other) const;
    bool operator!=(const mesh_iterator& other) const;
  };

This class template implements a generator for mesh graphs, suitable
for initializing an adjacency_list or other graph structure with
iterator-based initialization.

Where Defined
-------------
<``boost/graph/mesh_graph_generator.hpp``>

Constructors
------------

::

  mesh_iterator();

Constructs a past-the-end iterator.

::

  mesh_iterator(vertices_size_type x, vertices_size_type y, 
                bool toroidal = true);

Constructs a mesh generator iterator that creates a graph with ``x``
rows and ``y`` columns.  When ``toroidal`` is true the boundaries of
the mesh are connected to form a torus.

Example
-------

::

  #include <boost/graph/adjacency_list.hpp>
  #include <boost/graph/mesh_graph_generator.hpp>

  typedef boost::adjacency_list<> Graph;
  typedef boost::mesh_iterator<Graph> MeshGen;

  int main()
  {
    // Create non-toroidal graph with 100 rows and 100 columns
    Graph g(MeshGen(100, 100, false), MeshGen(), 10000);
    return 0;
  }


-----------------------------------------------------------------------------

Copyright (C) 2009 The Trustees of Indiana University.

Authors: Nick Edmonds and Andrew Lumsdaine

.. |Logo| image:: http://www.osl.iu.edu/research/pbgl/images/pbgl-logo.png
            :align: middle
            :alt: Parallel BGL
            :target: http://www.osl.iu.edu/research/pbgl
