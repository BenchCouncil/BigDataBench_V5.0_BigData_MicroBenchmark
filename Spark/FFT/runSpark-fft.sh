#!/bin/bash
  
# Running command: ./runSpark-fft.sh <row> <col> <sparsity>
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

echo $max 

log_value=`echo $max | awk '{print log($0)/log(2)}'`
log_value=$((${log_value//.*/+1}))


#-----------------------------run-workload-----------------------------#
echo "running fft"
$SPARK_HOME/bin/spark-submit --class fft.fft transform-operation/spark-fft/out/artifacts/spark_fft_jar/spark-fft.jar /spark/fft/fft-$row-$col-$sparsity /spark/fft/output $log_value

echo "spark fft end"
