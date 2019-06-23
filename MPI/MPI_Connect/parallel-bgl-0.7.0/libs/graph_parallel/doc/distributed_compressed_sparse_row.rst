.. Copyright (C) 2004-2009 The Trustees of Indiana University.
   Use, modification and distribution is subject to the Boost Software
   License, Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
   http://www.boost.org/LICENSE_1_0.txt)

========================================
|Logo| Distributed Compressed Sparse Row
========================================

.. contents::

Introduction
------------

The distributed compressed sparse row (CSR) graph implements a graph
data structure using the Boost Graph Library's
compressed_sparse_row_graph_ class template as its underlying storage
format.  The distributed CSR graph partitions the vertices of the
graph across several processes (which need not share an address
space). Edges outgoing from any vertex stored by a process are also
stored on that process.  The distributed CSR graph can currently only
be used with directed graphs.

Many features defined in this document depend on the new BGL CSR
interface, enabled by ``#define BOOST_GRAPH_USE_NEW_CSR_INTERFACE``.
The deprecated distributed CSR interface may still be accessed by not
defining ``BOOST_GRAPH_USE_NEW_CSR_INTERFACE`` but this interface will
eventually be removed in subsequent releases.  This deprecated
interface only supports static, directed graphs constructed from a
sorted edge range.

Distributed Vertex and Edge Properties
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Vertex and edge properties for distributed graphs mirror their
counterparts for non-distributed graphs. With a distributed graph,
however, vertex and edge properties are stored only in the process
that owns the vertex or edge. 

The distributed compressed sparse row graph only supports bundled
properties internally.  Vertex and edge properties are the second and
third template parameters respectively to the distributed compressed
sparse row graph.

Building a Distributed Graph
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Construction of distributed graphs can be slightly more
complicated than construction of normal, non-distributed graph data
structures, because one must account for both the distribution of the
vertices and edges and the multiple processes executing
concurrently. There are four main ways to construct distributed
graphs:

1. *A sorted edge range*: Constructing from a sorted edge range allows
the most efficient construction of a distributed CSR graph and does
not require additional temporary storage.  This edge range must
contain all edges in the graph and be equivalent on all processes.

2. *An unsorted edge range defined by an InputIterator_*: Constructing
from an unsorted edge range requires *O(E)* temporary storage to copy
and sort the edge range provided.  This edge range must contain all
edges in the graph and be equivalent on all processes.  The edge range
will be filtered before sorting and will only require an amount of
temporary storage proportional to the number of local edges.

3. *An unsorted edge range defined by a MultiPassInputIterator*: If
the unsorted edge range iterators model the MultiPassInputIterator_
concept then a distributed CSR graph can be constructed from an
unsorted edge range with no temporary storage.  This edge range must
contain all edges in the graph and be equivalent on all processes.

4. *In-place construction from vectors of sources and targets (and
properties)*: A distributed CSR graph can be constructed from vectors
of sources and targets.  This construction is termed *in-place*
because the storage for the targets vector is re-used and stores the
edges of the CSR graph.  This construction method is useful to
construct graphs which are large relative the the available memory as
it has very low storage overhead.  The in-place nature of the
construction does necessitate many swaps on individual data elements
to perform the sorting operations required, and thust this method of
construction may be significantly slower than other methods.  The
vectors of sources and targets must contain only the portion of the
edges in the graph that are local to the calling process.

Graph Concepts
~~~~~~~~~~~~~~

The distributed compressed sparse row graph models the Graph_ concept,
and in particular the `Distributed Graph`_ concept. It also models the
`Incidence Graph`_ and `Adjacency Graph`_ concept, but restricts the
input domain of the operations to correspond to local vertices
only. For instance, a process may only access the outgoing edges of a
vertex if that vertex is stored in that process.  The distributed
compressed sparse row graph can only be used with directed graphs,
undirected and bidirectional edge categories are not supported.
Distributed compressed sparse row graphs also model the `Mutable
Graph`_ concept (with edge insertion being *O(E)* and domain
restrictions; see the Reference_ section), `Property Graph`_ concept,
and `Mutable Property Graph`_ concept.

Unlike its non-distributed counterpart, the distributed adjacency list
does **not** model the `Vertex List Graph`_ or `Edge List Graph`_
concepts, because one cannot enumerate all vertices or edges within a
distributed graph. Instead, it models the weaker `Distributed Vertex
List Graph`_ and `Distributed Edge List Graph`_ concepts, which permit
access to the local edges and vertices on each processor. Note that if
all processes within the process group over which the graph is
distributed iterate over their respective vertex or edge lists, all
vertices and edges will be covered once.

Reference
---------

Since the distributed compressed sparse row graph closely follows the
non-distributed compressed_sparse_row_graph_, this reference documentation
only describes where the two implementations differ.

Where Defined
~~~~~~~~~~~~~

<boost/graph/distributed/compressed_sparse_row.hpp>

Associated Types
~~~~~~~~~~~~~~~~

::

  compressed_sparse_row_graph::process_group_type

The type of the process group over which the graph will be
distributed.

-----------------------------------------------------------------------------

::

  compressed_sparse_row_graph::distribution_type

The type of distribution used to partition vertices in the graph.

Member Functions
----------------

::

    compressed_sparse_row_graph(const ProcessGroup& pg = ProcessGroup());

    compressed_sparse_row_graph(vertices_size_type numverts,
			        const ProcessGroup& pg = ProcessGroup());

Construct an empty distributed CSR graph with the given process group
(or default) and graph property (or default).  Note that none of the
constructors for the CSR graph currently store graph properties,
constructors containing graph properties are only included for the
sake of completeness and compatibility with other graph data
structures. Full functionality for graph properties will be
implemented in the future.

-----------------------------------------------------------------------------

::

    compressed_sparse_row_graph(vertices_size_type numverts,
	 		        const ProcessGroup& pg = ProcessGroup());

    compressed_sparse_row_graph(vertices_size_type numverts,
	  		        const GraphProperty& prop,
			        const ProcessGroup& pg = ProcessGroup());

    template <typename Distribution>
    compressed_sparse_row_graph(vertices_size_type numverts,
			        const ProcessGroup& pg,
			        const Distribution& dist);

    template <typename Distribution>
    compressed_sparse_row_graph(vertices_size_type numverts,
	 		        const GraphProperty& prop,
			        const ProcessGroup& pg,
			        const Distribution& dist);

Construct a distributed CSR graph with ``n`` vertices, optionally
providing the graph property, process group, or distribution. The
``n`` vertices will be distributed via the given (or
default-constructed) distribution. This operation is collective,
requiring all processes with the process group to execute it
concurrently.

-----------------------------------------------------------------------------

:: 

    template <typename EdgeIterator>
    compressed_sparse_row_graph(edges_are_unsorted_t,
			        EdgeIterator edge_begin, EdgeIterator edge_end,
                                vertices_size_type numverts,
                                const ProcessGroup& pg = ProcessGroup(),
                                const GraphProperty& prop = GraphProperty());

    template <typename EdgeIterator, typename Distribution>
    compressed_sparse_row_graph(edges_are_unsorted_t,
			        EdgeIterator edge_begin, EdgeIterator edge_end,
                                vertices_size_type numverts,
                                const ProcessGroup& pg,
			        const Distribution& dist,
                                const GraphProperty& prop = GraphProperty());

    template <typename EdgeIterator, typename EdgePropertyIterator>
    compressed_sparse_row_graph(edges_are_unsorted_t,
			        EdgeIterator edge_begin, EdgeIterator edge_end,
                                EdgePropertyIterator ep_iter,
                                vertices_size_type numverts,
                                const ProcessGroup& pg = ProcessGroup(),
                                const GraphProperty& prop = GraphProperty());

    template <typename EdgeIterator, typename EdgePropertyIterator,
	      typename Distribution>
    compressed_sparse_row_graph(edges_are_unsorted_t,
	 		        EdgeIterator edge_begin, EdgeIterator edge_end,
                                EdgePropertyIterator ep_iter,
                                vertices_size_type numverts,
                                const ProcessGroup& pg,
			        const Distribution& dist,
                                const GraphProperty& prop = GraphProperty());

    template <typename EdgeIterator>
    compressed_sparse_row_graph(edges_are_sorted_t,
			        EdgeIterator edge_begin, EdgeIterator edge_end,
                                vertices_size_type numverts,
			        edges_size_type numedges = 0,
                                const ProcessGroup& pg = ProcessGroup(),
                                const GraphProperty& prop = GraphProperty());

    template <typename EdgeIterator, typename Distribution>
    compressed_sparse_row_graph(edges_are_sorted_t,
			        EdgeIterator edge_begin, EdgeIterator edge_end,
                                vertices_size_type numverts,
                                const ProcessGroup& pg,
			        const Distribution& dist,
                                const GraphProperty& prop = GraphProperty());

    template <typename EdgeIterator, typename EdgePropertyIterator>
    compressed_sparse_row_graph(edges_are_sorted_t,
			        EdgeIterator edge_begin, EdgeIterator edge_end,
                                EdgePropertyIterator ep_iter,
                                vertices_size_type numverts,
			        edges_size_type numedges = 0,
                                const ProcessGroup& pg = ProcessGroup(),
                                const GraphProperty& prop = GraphProperty());

    template <typename EdgeIterator, typename EdgePropertyIterator,
	      typename Distribution>
    compressed_sparse_row_graph(edges_are_sorted_t,
			        EdgeIterator edge_begin, EdgeIterator edge_end,
                                EdgePropertyIterator ep_iter,
                                vertices_size_type numverts,
                                const ProcessGroup& pg,
			        const Distribution& dist,
                                const GraphProperty& prop = GraphProperty());

    template <typename EdgeIterator>
    compressed_sparse_row_graph(edges_are_unsorted_multi_pass_t,
			        EdgeIterator edge_begin, 
			        EdgeIterator edge_end,
			        vertices_size_type numverts,
                                const ProcessGroup& pg = ProcessGroup(),
                                const GraphProperty& prop = GraphProperty());

    template <typename EdgeIterator, typename Distribution>
    compressed_sparse_row_graph(edges_are_unsorted_multi_pass_t,
			        EdgeIterator edge_begin, 
			        EdgeIterator edge_end,
			        vertices_size_type numverts,
                                const ProcessGroup& pg,
			        const Distribution& dist,
                                const GraphProperty& prop = GraphProperty());

    template <typename EdgeIterator, typename EdgePropertyIterator>
    compressed_sparse_row_graph(edges_are_unsorted_multi_pass_t,
			        EdgeIterator edge_begin, 
			        EdgeIterator edge_end,
			        EdgePropertyIterator ep_iter,
			        vertices_size_type numverts,
                                const ProcessGroup& pg = ProcessGroup(),
                                const GraphProperty& prop = GraphProperty());

    template <typename EdgeIterator, typename EdgePropertyIterator,
	      typename Distribution>
    compressed_sparse_row_graph(edges_are_unsorted_multi_pass_t,
			        EdgeIterator edge_begin, 
			        EdgeIterator edge_end,
			        EdgePropertyIterator ep_iter,
			        vertices_size_type numverts,
                                const ProcessGroup& pg,
			        const Distribution& dist,
                                const GraphProperty& prop = GraphProperty());

Construct a distributed CSR graph with ``n`` vertices and with edges
specified in the edge list given by the range ``[first, last)``. The
``EdgeIterator`` must be a model of InputIterator_ (and
MultiPassInputIterator_ for constructors that include ``multi_pass``
in their tag). The value type of the ``EdgeIterator`` must be a
``std::pair``, where the type in the pair is an integer type. The
integers will correspond to vertices, and they must all fall in the
range of ``[0, n)``. When provided, ``ep_iter`` refers to an edge
property list ``[ep_iter, ep_iter + m)`` contains properties for each
of the edges.

These constructors are collective operations and must be executed
concurrently by each process with the same argument list. Most
importantly, the edge lists provided to the constructor in each
process should be equivalent. The vertices will be partitioned among
the processes according to the ``distribution``, with edges placed on
the process owning the source of the edge. Note that this behavior
permits sequential graph construction code to be parallelized
automatically, regardless of the underlying distribution.

Note that only the '*tagged*' constructors are documented.  Untagged
constructors are deprecated and will be removed in subsequent
releasese.  The first argument to all of the subsequent constructors
are tags used to distinguish amongst them.  Each tag has a
corresponding value that can be used to initialize the tag parameter.
For example ``edges_are_sorted`` can be used to initialize the
``edges_are_sorted_t`` tag.

-----------------------------------------------------------------------------

::

    template <typename Source>
    compressed_sparse_row_graph(distributed_construct_inplace_from_sources_and_targets_t,
	 		        std::vector<Source>& sources,
			        std::vector<vertex_descriptor>& targets,
			        vertices_size_type numverts,
                                const ProcessGroup& pg = ProcessGroup(),
                                const GraphProperty& prop = GraphProperty());

    template <typename Distribution, typename Source> 
    compressed_sparse_row_graph(distributed_construct_inplace_from_sources_and_targets_t,
			        std::vector<Source>& sources,
			        std::vector<vertex_descriptor>& targets,
			        vertices_size_type numverts,
                                const ProcessGroup& pg,
			        const Distribution& dist,
                                const GraphProperty& prop = GraphProperty());

    template <typename Source>
    compressed_sparse_row_graph(distributed_construct_inplace_from_sources_and_targets_t,
			        std::vector<Source>& sources,
			        std::vector<vertex_descriptor>& targets,
			        std::vector<edge_bundled>& edge_props,
			        vertices_size_type numverts,
                                const ProcessGroup& pg = ProcessGroup(),
                                const GraphProperty& prop = GraphProperty());

    template <typename Distribution, typename Source>
    compressed_sparse_row_graph(distributed_construct_inplace_from_sources_and_targets_t,
			        std::vector<Source>& sources,
			        std::vector<vertex_descriptor>& targets,
			        std::vector<edge_bundled>& edge_props,
			        vertices_size_type numverts,
                                const ProcessGroup& pg,
			        const Distribution& dist,
                                const GraphProperty& prop = GraphProperty());

Construct a distributed CSR graph with ``n`` vertices and with edges
specified in the edge list given by the ``source`` and ``target``
vectors.  The value type of the ``targets`` vector must be the vertex
descriptor type of the graph.  The value type of the ``sources``
vector should be convertible to the vertex descriptor type of the
graph, but it is templated to allow for using 32-bit local indices in
lieu of the (larger) vertex descriptor type to make it possible to
conserve space when loading very large graphs.  The edge properties
(where specified) are contained in ``edge_props``.  When present the
edge properties must be bundled properties.  ``sources``, ``targets``,
and ``edge_props`` must all have the same length.

These constructors are collective operations and must be executed
concurrently by each process.  The distribution of vertices and edges
will be determined by the contents of the edge list on each process as
defined by the ``sources`` and ``targets`` vector.

-----------------------------------------------------------------------------

::

  process_group_type& process_group();
  const process_group_type& process_group() const;

Returns the process group over which this graph is distributed.

-----------------------------------------------------------------------------

::

  distribution_type&       distribution();
  const distribution_type& distribution() const;

Returns the distribution used for initial placement of vertices.

-----------------------------------------------------------------------------

::

  base_type&       base();
  const base_type& base() const;

Returns the BGL CSR graph used to store edges and vertices internally

-----------------------------------------------------------------------------

::

  vertex_descriptor 
  make_vertex_descriptor(process_id_type p, vertex_descriptor v) const;

Create a vertex descriptor from a process ID and a local index.

-----------------------------------------------------------------------------

::

  vertex_descriptor local_to_global_vertex(vertex_descriptor v) const;

Convert a local vertex descriptor into a global vertex descriptor.


Non-Member Functions
~~~~~~~~~~~~~~~~~~~~

::

  std::pair<vertex_iterator, vertex_iterator>
  vertices(const adjacency_list& g);

Returns an iterator-range providing access to the vertex set of graph
``g`` stored in this process. Each of the processes that contain ``g``
will get disjoint sets of vertices.

-----------------------------------------------------------------------------

::

  std::pair<edge_iterator, edge_iterator>
  edges(const compressed_sparse_row_graph& g);

Returns an iterator-range providing access to the edge set of graph
``g`` stored in this process.

-----------------------------------------------------------------------------

::

  std::pair<adjacency_iterator, adjacency_iterator>
  adjacent_vertices(vertex_descriptor u, const compressed_sparse_row_graph& g);

Returns an iterator-range providing access to the vertices adjacent to
vertex ``u`` in graph ``g``. The vertex ``u`` must be local to this process.

-----------------------------------------------------------------------------

::

  std::pair<out_edge_iterator, out_edge_iterator>
  out_edges(vertex_descriptor u, const compressed_sparse_row_graph& g);

Returns an iterator-range providing access to the out-edges of vertex
``u`` in graph ``g``. For an out-edge ``e``, ``source(e, g) == u`` and
``target(e, g) == v`` where ``v`` is a vertex adjacent to ``u``. The
vertex ``u`` must be local to this process.

-----------------------------------------------------------------------------

::

  degree_size_type
  out_degree(vertex_descriptor u, const compressed_sparse_row_graph& g);

Returns the number of edges leaving vertex ``u``. Vertex ``u`` must
be local to the executing process.

-----------------------------------------------------------------------------

::

  vertices_size_type
  num_vertices(const compressed_sparse_row_graph& g);

Returns the number of vertices in the graph ``g`` that are stored in
the executing process.

-----------------------------------------------------------------------------

::

  edges_size_type
  num_edges(const compressed_sparse_row_graph& g);

Returns the number of edges in the graph ``g`` that are stored in the
executing process.

-----------------------------------------------------------------------------

::

  vertex_descriptor
  vertex(vertices_size_type n, const compressed_sparse_row_graph& g);

Returns the ``n`` th vertex in the graph's vertex list, according to the
distribution used to partition the graph. ``n`` must be a value
between zero and the sum of ``num_vertices(g)`` in each process (minus
one). Note that it is not necessarily the case that ``vertex(0, g) ==
*num_vertices(g).first``. This function is only guaranteed to be
accurate when no vertices have been added to or removed from the
graph after its initial construction.

-----------------------------------------------------------------------------

::

  std::pair<edge_descriptor, bool>
  edge(vertex_descriptor u, vertex_descriptor v,
       const compressed_sparse_row_graph& g);

Returns an edge connecting vertex ``u`` to vertex ``v`` in graph ``g``
if one exists.  If no edge ``(u,v)`` exists a default-constructed
``edge_descriptor`` will be returned and the ``bool`` flag will be
``false``.  Vertex ``u`` must be local.

-----------------------------------------------------------------------------

::

  std::pair<out_edge_iterator, out_edge_iterator>
  edge_range(vertex_descriptor u, vertex_descriptor v,
             const compressed_sparse_row_graph& g);

Returns a pair of out-edge iterators that give the range for all the
parallel edges from ``u`` to ``v``. Vertex ``u`` must be stored in the
executing process.


Structure Modification
~~~~~~~~~~~~~~~~~~~~~~

::

  vertex_descriptor add_vertex(compressed_sparse_row_graph& g);

Adds a vertex to the graph and returns the vertex descriptor for the
new vertex. The vertex will be stored in the local process.

-----------------------------------------------------------------------------

::

  vertex_descriptor add_vertices(vertices_size_type count, 
                                 compressed_sparse_row_graph& g);

Adds ``count`` vertices to the graph and returns the vertex descriptor for the
first new vertex. The vertices will be stored in the local process.

-----------------------------------------------------------------------------

::
  
  template <typename EdgeIterator>
  void add_edges(EdgeIterator first, EdgeIterator last,
                 compressed_sparse_row_graph& g);

Add the edges in the edge list defined by ``[first, last)`` to the
graph ``g``.  The source of each edge in ``[first, last)`` must be
stored in the local process. The ``EdgeIterator`` must be a model of
InputIterator_.  The value type of ``EdgeIterator`` must be
``std::pair`` where the elements in the pair are vertex descriptors in
the graph ``g``.  Temporary space proportional to the number of edges
added is required to sort the edges prior to insertion.

-----------------------------------------------------------------------------

::
  
  template <typename EdgeIterator, typename EdgePropertyIterator>
  void add_edges(EdgeIterator first, EdgeIterator last,
                 EdgePropertyIterator ep_iter,
                 compressed_sparse_row_graph& g);

Add the edges in the edge list defined by ``[first, last)`` to the
graph ``g``.  ``ep_iter`` refers to an edge property list ``[ep_iter,
ep_iter + m)`` contains properties for each of the edges where ``m``
is the number of edges in the range [first, last). The source of each
edge in ``[first, last)`` must be stored in the local process. The
``EdgeIterator`` must be a model of InputIterator_.  The value type of
``EdgeIterator`` must be ``std::pair`` where the elements in the pair
are vertex descriptors in the graph ``g``. The value type of
``EdgePropertyIterator`` must be the ``edge_bundled`` type of the
graph.  Temporary space proportional to the number of edges added is
required to sort the edges prior to insertion.

-----------------------------------------------------------------------------

::
  
  template <typename EdgeIterator>
  void add_edges_sorted(EdgeIterator first, EdgeIterator last,
                        compressed_sparse_row_graph& g);

Add the edges in the edge list defined by ``[first, last)`` to the
graph ``g``.  The source of each edge in ``[first, last)`` must be
stored in the local process. The ``EdgeIterator`` must be a model of
BidirectionalIterator_.  The value type of ``EdgeIterator`` must be
``std::pair`` where the elements in the pair are vertex descriptors in
the graph ``g``.

-----------------------------------------------------------------------------

::
  
  template <typename EdgeIterator, typename EdgePropertyIterator>
  void add_edges_sorted(EdgeIterator first, EdgeIterator last,
                        EdgePropertyIterator ep_iter,
                        compressed_sparse_row_graph& g);

Add the edges in the edge list defined by ``[first, last)`` to the
graph ``g``.  ``ep_iter`` refers to an edge property list ``[ep_iter,
ep_iter + m)`` contains properties for each of the edges where ``m``
is the number of edges in the range ``[first, last)``. The source of
each edge in ``[first, last)`` must be stored in the local
process. The ``EdgeIterator`` must be a model of
BidirectionalIterator_. The value type of ``EdgeIterator`` must be
``std::pair`` where the elements in the pair are vertex descriptors in
the graph ``g``. The value type of ``EdgePropertyIterator`` must be
the ``edge_bundled`` type of the graph.

-----------------------------------------------------------------------------

Copyright (C) 2004 The Trustees of Indiana University.

Copyright (C) 2009 Nick Edmonds

Authors: Nick Edmonds and Andrew Lumsdaine

.. |Logo| image:: http://www.osl.iu.edu/research/pbgl/images/pbgl-logo.png
            :align: middle
            :alt: Parallel BGL
            :target: http://www.osl.iu.edu/research/pbgl

.. _compressed_sparse_row_graph: http://www.boost.org/libs/graph/doc/compressed_sparse_row.html
.. _InputIterator: http://www.boost.org/doc/html/InputIterator.html
.. _BidirectionalIterator: http://www.boost.org/doc/html/BidirectionalIterator.html
.. _MultiPassInputIterator: http://www.boost.org/libs/utility/doc/MultiPassInputIterator.html
.. _Incidence Graph: http://www.boost.org/libs/graph/doc/IncidenceGraph.html
.. _Adjacency Graph: http://www.boost.org/libs/graph/doc/AdjacencyGraph.html
.. _Bidirectional Graph: http://www.boost.org/libs/graph/doc/BidirectionalGraph.html
.. _Mutable Graph: http://www.boost.org/libs/graph/doc/MutableGraph.html
.. _Property Graph: http://www.boost.org/libs/graph/doc/PropertyGraph.html
.. _Mutable Property Graph: http://www.boost.org/libs/graph/doc/MutablePropertyGraph.html
.. _Vertex List Graph: http://www.boost.org/libs/graph/doc/VertexListGraph.html
.. _Edge List Graph: http://www.boost.org/libs/graph/doc/EdgeListGraph.html
.. _Distribution: concepts/Distribution.html
.. _Graph: http://www.boost.org/libs/graph/doc/Graph.html
.. _Distributed graph: DistributedGraph.html
.. _Distributed Vertex List Graph: DistributedVertexListGraph.html
.. _Distributed Edge List Graph: DistributedEdgeListGraph.html
