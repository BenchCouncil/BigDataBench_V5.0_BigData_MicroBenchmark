#!/bin/bash

# Running command: ./runSpark-grep.sh <size>
#       size: the input data size, GB
  
curdir=`pwd`
a=$1

#-----------------------------run-workload-----------------------------#
echo "running spark grep"
$SPARK_HOME/bin/spark-submit --class cn.ac.ict.bigdatabench.Grep bigdatabench-spark_1.3.0-hadoop_1.0.4.jar /spark/grep/grep-$a"G" xyz spark/grep/output

echo "spark grep end"
