#!/bin/bash

# Running command: ./run-grep.sh <size>
#	size: the input data size, GB
  
a=$1

#-----------------------------run-workload-----------------------------#
echo "running hadoop grep"
${HADOOP_HOME}/bin/hadoop fs -rmr /hadoop/grep/grepHP-result
${HADOOP_HOME}/bin/hadoop jar ${HADOOP_HOME}/share/hadoop/mapreduce/hadoop-mapreduce-examples-*.jar grep  /hadoop/grep/$a"GB"-grepHP  /hadoop/grep/grepHP-result xyz

echo "hadop grep end"
