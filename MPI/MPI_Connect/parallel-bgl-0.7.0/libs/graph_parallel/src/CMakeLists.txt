add_library(boost_graph_mpi mpi_process_group.cpp tag_allocator.cpp)
set_target_properties(boost_graph_mpi
  PROPERTIES LINK_FLAGS "${MPI_LINK_FLAGS}"
             COMPILE_FLAGS "${MPI_COMPILE_FLAGS}"
             SOVERSION ${PBGL_VERSION})
target_link_libraries(boost_graph_mpi ${Boost_LIBRARIES} ${MPI_LIBRARIES})

install(TARGETS boost_graph_mpi
        DESTINATION lib
        EXPORT pbgl-targets)
install(EXPORT pbgl-targets
  DESTINATION lib/pbgl-${PBGL_VERSION})