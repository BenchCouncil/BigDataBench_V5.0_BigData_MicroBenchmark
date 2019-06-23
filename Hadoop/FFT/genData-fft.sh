#!/bin/bash

# Generating command: ./genData-fft.sh <row> <col> <sparsity>
#	row: the row number of the matrix
#	col: the column number of the matrix
#	sparsity: the percentage of zero element, ranges from 0 to 1.
  
#----------------------------genenrate-data----------------------------#
curdir=`pwd`
#--------------generate-data-----------------------
row=$1
col=$2
sparsity=$3

if [ $row -gt $col ];then
    max=$row
else
    max=$col
fi

echo $max 

cd genData-Matrix
rm -f data-kmeans
make
sh generate-matrix.sh float $row $col $sparsity
$HADOOP_HOME/bin/hadoop fs -rmr /hadoop/fft/
$HADOOP_HOME/bin/hadoop fs -mkdir -p /hadoop/fft/
$HADOOP_HOME/bin/hadoop fs -put data-kmeans /hadoop/fft/fft-$row-$col-$sparsity

