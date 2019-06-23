#!/bin/bash

# Generating command: ./genData-matMult.sh <sparsity> <row_i> <col_i> <col_j> 
#	sparsity: the percentage of zero elements, ranges from 0 to 1.
#	row_i: the row number of matrix A
#	col_i: the column number of matrix A
#	col_j: the column number of matrix B
  
#----------------------------genenrate-data----------------------------#
sparsity=$1
row_i=$2
col_i=$3
col_j=$4

cd genData-Matrix
rm -f data-kmeans
make
sh generate-matrix.sh int $row_i $col_i $sparsity
mv data-kmeans mat1
sh generate-matrix.sh int $col_i $col_j $sparsity
mv data-kmeans mat2
$HADOOP_HOME/bin/hadoop fs -rmr /hadoop/matMult/
$HADOOP_HOME/bin/hadoop fs -mkdir -p /hadoop/matMult/
$HADOOP_HOME/bin/hadoop fs -put mat* /hadoop/matMult/

