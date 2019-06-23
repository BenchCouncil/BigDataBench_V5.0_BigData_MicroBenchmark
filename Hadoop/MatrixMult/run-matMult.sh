#!/bin/bash

# Running command: ./run-matMult.sh <sparsity> <row_i> <col_i> <col_j> 
#       sparsity: the percentage of zero elements, ranges from 0 to 1.
#       row_i: the row number of matrix A
#       col_i: the column number of matrix A
#       col_j: the column number of matrix B
  
sparsity=$1
row_i=$2
col_i=$3
col_j=$4

#-----------------------------run-workload-----------------------------#
echo "running matMult"
${HADOOP_HOME}/bin/hadoop fs -rmr /hadoop/matMult/mat*-seq /hadoop/matMult/mat-out

$MAHOUT_HOME/bin/mahout seqdirectory --input /hadoop/matMult/mat1 --output /hadoop/matMult/mat1-seq
$MAHOUT_HOME/bin/mahout seqdirectory --input /hadoop/matMult/mat2 --output /hadoop/matMult/mat2-seq

${MAHOUT_HOME}/bin/mahout matrixmult \
        --numRowsA $row_i \
        --numColsA $col_i \
        --numRowsB $col_i \
        --numColsB $col_j \
        --inputPathA /hadoop/matMult/mat1-seq \
        --inputPathB /hadoop/matMult/mat2-seq \
        --outputPath /hadoop/matMult/mat-out

echo "hadoop matrix mulitiply end"
