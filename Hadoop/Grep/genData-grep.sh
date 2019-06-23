#!/bin/bash

# Generating command: ./genData-grep.sh <size>
#	size: the input data size, GB
  
#----------------------------genenrate-data----------------------------#
curdir=`pwd`
a=$1

    let L=a*2
    cd ../../BigDataGeneratorSuite/Text_datagen/
    rm -fr ./gen_data/$a"GB"-grepHP
    ./gen_text_data.sh lda_wiki1w $L 8000 10000 ./gen_data/$a"GB"-grepHP
    ${HADOOP_HOME}/bin/hadoop fs -rmr /hadoop/grep/$a"GB"-grepHP
    ${HADOOP_HOME}/bin/hadoop fs -mkdir -p /hadoop/grep/
    ${HADOOP_HOME}/bin/hadoop fs -put ./gen_data/$a"GB"-grepHP /hadoop/grep/

