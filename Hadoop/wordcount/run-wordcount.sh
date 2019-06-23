#!/bin/bash

# Running command: ./run-wordcount.sh <size>
#	size: the input data size, GB
  
a=$1

#-----------------------------run-workload-----------------------------#
echo "running wordcount"
cd $curdir
cd ./externals/shell/industryPack/hadoop/workloads/wordcount 
${HADOOP_HOME}/bin/hadoop fs -rmr /hadoop/wd/wordcountHP-result
${HADOOP_HOME}/bin/hadoop jar ${HADOOP_HOME}/share/hadoop/mapreduce/hadoop-mapreduce-examples-*.jar  wordcount  /hadoop/wd/$a"GB"-wordcountHP  /hadoop/wd/wordcountHP-result


#-----------------killing monitor script--------------
echo "hadoop wordcount end"
