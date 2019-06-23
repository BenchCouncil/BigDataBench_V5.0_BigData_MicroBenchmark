#!/bin/bash

# Running command: ./run-cc.sh <log_vertex>
#	log_vertex: indicates the vertex of the input data, means vertex = 2^log_vertex

  
reducers=12
I=$1

#--------------------------------------------run----------------------------#
${HADOOP_HOME}/bin/hadoop fs -rmr concmpt_curbm concmpt_tempbm concmpt_nextbm concmpt_output
${HADOOP_HOME}/bin/hadoop jar pegasus-2.0.jar pegasus.ConCmpt -D mapred.input.format.class=org.apache.hadoop.mapred.lib.NLineInputFormat -D mapred.line.input.format.linespermap=2500000 /hadoop/cc/Google_genGraph_$I.txt concmpt_curbm concmpt_tempbm concmpt_nextbm concmpt_output $I $reducers new makesym

echo "hadoop cc end"
