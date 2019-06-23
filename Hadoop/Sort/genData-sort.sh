#!/bin/bash
  
# Generating command: ./genData-sort.sh <size>
#	size: the input data size, GB

a=$1
let L=$a*10000000

#-----------------generating input data---------------
$HADOOP_HOME/bin/hadoop dfs -rmr /hadoop/terasort/terasort-${a}G
$HADOOP_HOME/bin/hadoop jar ${HADOOP_HOME}/share/hadoop/mapreduce/hadoop-mapreduce-examples-*.jar teragen $L /hadoop/terasort/terasort-${a}G
$HADOOP_HOME/bin/hadoop dfs -rmr /hadoop/terasort/terasort-out

