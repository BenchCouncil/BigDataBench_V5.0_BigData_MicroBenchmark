#!/bin/bash

# Generating command: ./genData-cc.sh <log_vertex>
#	log_vertex: indicates the vertex of the generated data, means vertex = 2^log_vertex

  
#----------------------------genenrate-data----------------------------#
curdir=`pwd`
I=$1

cd ../../BigDataGeneratorSuite/Graph_datagen
dir=/hadoop/cc

rm -fr ./gen_data/Google_genGraph_$I.txt
./gen_kronecker_graph  -o:./gen_data/Google_genGraph_$I.txt -m:"0.8305 0.5573; 0.4638 0.3021" -i:$I
head ./gen_data/Google_genGraph_$I.txt > ./gen_data/Google_parameters_$I
sed 1,4d ./gen_data/Google_genGraph_$I.txt > ./gen_data/Google_genGraph_$I.tmp

mv ./gen_data/Google_genGraph_$I.tmp ./gen_data/Google_genGraph_$I.txt

${HADOOP_HOME}/bin/hadoop fs -rmr /hadoop/cc/Google_genGraph_$I.txt
${HADOOP_HOME}/bin/hadoop fs -mkdir -p /hadoop/cc
${HADOOP_HOME}/bin/hadoop fs -put ./gen_data/Google_genGraph_$I.txt /hadoop/cc

