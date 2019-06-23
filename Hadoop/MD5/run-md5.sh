#!/bin/bash

# Running command: ./run-md5.sh <size>
#	size: the input data size, GB
  
a=$1

#-----------------------------run-workload-----------------------------#
echo "running wordcount"
${HADOOP_HOME}/bin/hadoop fs -rmr /hadoop/md5/md5HP-result
$HADOOP_HOME/bin/hadoop jar md5/DwarfMD5.jar DwarfMD5 /hadoop/md5/$a"GB"-md5HP /hadoop/md5/md5HP-result

echo "hadoop md5 end"
