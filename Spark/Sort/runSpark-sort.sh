#!/bin/bash

# Running command: ./runSpark-sort.sh <size>
#	size: the input data size, GB
  
a=$1

#-----------------------------run-workload-----------------------------#
echo "running wordcount"
$SPARK_HOME/bin/spark-submit --class cn.ac.ict.bigdatabench.Sort --conf spark.driver.maxResultSize=4g bigdatabench-spark_1.3.0-hadoop_1.0.4.jar /spark/sort/sort-$a"G" /spark/sort/output

echo "spark sort end"
