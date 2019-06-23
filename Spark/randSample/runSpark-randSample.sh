#!/bin/bash

# Running command: ./runSpark-randSample.sh <size> <sample_ratio>
#	size: the input data size, GB
#	sample_ratio: the sampling ratio,ranges from 0 to 1.
  
a=$1

#-----------------------------run-workload-----------------------------#
echo "running randsample"
$SPARK_HOME/bin/spark-submit --class com.RandSample.RandSample RandSample-spark/target/RandSample-1.5.2-spark.jar /spark/randsample/$a"GB"-randsample /spark/randsample/output $2 123

#-----------------killing monitor script--------------
echo "spark randsample end"
