#!/bin/bash

# Running command: ./run-fft.sh <row> <col> <sparsity>
#       row: the row number of the matrix
#       col: the column number of the matrix
#       sparsity: the percentage of zero element, ranges from 0 to 1.
  
curdir=`pwd`
row=$1
col=$2
sparsity=$3

if [ $row -gt $col ];then
    max=$row
else
    max=$col
fi

log_value=`echo $max | awk '{print log($0)/log(2)}'`
log_value=$((${log_value//.*/+1}))
echo $log_value

#-----------------------------run-workload-----------------------------#
echo "running fft"
${HADOOP_HOME}/bin/hadoop fs -rmr /hadoop/fft/fft-result*
${HADOOP_HOME}/bin/hadoop jar fft/src/main/java/org/fft/fft.jar org.fft.fft /hadoop/fft/fft-$row-$col-$sparsity /hadoop/fft/fft-result1 /hadoop/fft/fft-result $log_value

#-----------------killing monitor script--------------
echo "hadoop fft end"
