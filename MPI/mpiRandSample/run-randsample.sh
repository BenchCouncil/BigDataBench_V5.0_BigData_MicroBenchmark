#!/bin/bash

mpirun -f ../nodefile -n 12 ./mpi-md5 data-randsample outdata
