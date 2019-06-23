#!/bin/bash
 
# Running command: ./runSpark-wordcount.sh <size>
#	size: the input data size, GB
 
a=$1
#-----------------------------run-workload-----------------------------#
echo "running wordcount"
$SPARK_HOME/bin/spark-submit --class cn.ac.ict.bigdatabench.WordCount bigdatabench-spark_1.3.0-hadoop_1.0.4.jar /spark/wordcount/wordcount-$a"G" /spark/wordcount/output

echo "spark wordcount end"
