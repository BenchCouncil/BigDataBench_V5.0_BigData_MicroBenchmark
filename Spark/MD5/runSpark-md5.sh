#!/bin/bash

# Running command: ./runSpark-md5.sh <size>
#	size: the input data size, GB
  
a=$1

#-----------------------------run-workload-----------------------------#
echo "running md5"
$SPARK_HOME/bin/spark-submit --class com.md5.MD5 md5/MD5-1.5.2-spark.jar /spark/md5/md5-$a"G" /spark/md5/output

echo "spark md5 end"
