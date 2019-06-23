#!/bin/bash

# Generating command: ./genData-fft.sh <row> <col> <sparsity>
#       row: the row number of the matrix
#       col: the column number of the matrix
#       sparsity: the percentage of zero element, ranges from 0 to 1.
  
#----------------------------genenrate-data----------------------------#
row=$1
col=$2
sparsity=$3

cd genData-Matrix
rm -f data-kmeans
make
sh generate-matrix.sh float $row $col $sparsity
$HADOOP_HOME/bin/hadoop fs -rmr /spark/fft/
$HADOOP_HOME/bin/hadoop fs -mkdir -p /spark/fft/
$HADOOP_HOME/bin/hadoop fs -put /spark/fft/fft-$row-$col-$sparsity
rm -f data-kmeans

