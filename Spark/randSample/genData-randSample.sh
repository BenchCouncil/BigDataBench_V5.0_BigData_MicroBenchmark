#!/bin/bash
 
# Generating command: ./genData-randSample.sh <size>
#	size: the input data size, GB
 
#----------------------------genenrate-data----------------------------#
curdir=`pwd`
a=$1

    let L=a*2
    cd ../../BigDataGeneratorSuite/Text_datagen/
    rm -fr ./gen_data/$a"GB"-randsample
    ./gen_text_data.sh lda_wiki1w $L 8000 10000 ./gen_data/$a"GB"-randsample
    ${HADOOP_HOME}/bin/hadoop fs -rmr /spark/randsample/*
    ${HADOOP_HOME}/bin/hadoop fs -mkdir -p /spark/randsample
    ${HADOOP_HOME}/bin/hadoop fs -put ./gen_data/$a"GB"-randsample /spark/randsample
rm -fr ./gen_data/$a"GB"-randsample
