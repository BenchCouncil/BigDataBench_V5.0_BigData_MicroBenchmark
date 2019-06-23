#!/bin/bash

# Running command: ./runSpark-matMult.sh <row_i> <col_i> <col_j>
#	row_i: the row number of matrix A
#	col_i: the column number of matrix A
#	col_j: the column number of matrix B
  
row_i=$2
col_i=$3
col_j=$4

#-----------------------------run-workload-----------------------------#
echo "running matMult"
$SPARK_HOME/bin/spark-submit --class edu.nju.pasalab.marlin.examples.MatrixMultiply matrix-operation/target/marlin-0.4-SNAPSHOT.jar $row_i $col_i $col_j 12

echo "spark matMult end"
