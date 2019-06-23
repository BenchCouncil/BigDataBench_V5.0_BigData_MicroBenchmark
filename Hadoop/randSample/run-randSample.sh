#!/bin/bash

# Running command: ./run-randSample.sh <size> <sample_ratio>
#	size: the input data size, GB
#	sample_ratio: the sampling ratio, ranges from 0 to 1.
  
curdir=`pwd`
a=$1

#-----------------------------run-workload-----------------------------#
echo "running randsample"
${HADOOP_HOME}/bin/hadoop fs -rmr /hadoop/randsample/randsampleHP-result
${HADOOP_HOME}/bin/hadoop jar RandSample/out/artifacts/RandSample_jar/RandSample.jar RandSample /hadoop/randsample/$a"GB"-randsampleHP  /hadoop/randsample/randsampleHP-result $2


#-----------------killing monitor script--------------
echo "hadoop rand sampling end"
