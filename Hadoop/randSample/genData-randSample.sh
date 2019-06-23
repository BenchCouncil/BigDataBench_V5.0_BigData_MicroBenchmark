#!/bin/bash

# Generating command: ./genData-randSample.sh <size>
#	size: the input data size, GB
 
#----------------------------genenrate-data----------------------------#
curdir=`pwd`
a=$1

    let L=a*2
    cd ../../BigDataGeneratorSuite/Text_datagen/
    rm -fr ./gen_data/$a"GB"-randsampleHP
    ./gen_text_data.sh lda_wiki1w $L 8000 10000 ./gen_data/$a"GB"-randsampleHP
    ${HADOOP_HOME}/bin/hadoop fs -rmr /hadoop/randsample/$a"GB"-randsampleHP
    ${HADOOP_HOME}/bin/hadoop fs -mkdir -p /hadoop/randsample
    ${HADOOP_HOME}/bin/hadoop fs -put ./gen_data/$a"GB"-randsampleHP /hadoop/randsample
