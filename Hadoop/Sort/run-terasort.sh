#!/bin/bash

# Running command: ./run-terasort.sh <size>
#	size: the input data size, GB
  
a=$1

#-----------------running hadoop terasort-------------
$HADOOP_HOME/bin/hadoop jar ${HADOOP_HOME}/share/hadoop/mapreduce/hadoop-mapreduce-examples-*.jar terasort /hadoop/terasort/terasort-${a}G /hadoop/terasort/terasort-out


#-----------------killing monitor script--------------
echo "terasort end, kill monitor script"
