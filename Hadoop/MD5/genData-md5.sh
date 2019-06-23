#!/bin/bash

# Generating command: ./genData-md5.sh <size>
#	size: the input data size, GB
  
#----------------------------genenrate-data----------------------------#
a=$1

    let L=a*2
    cd ../../BigDataGeneratorSuite/Text_datagen/
    rm -fr ./gen_data/$a"GB"-md5HP
    ./gen_text_data.sh lda_wiki1w $L 8000 10000 ./gen_data/$a"GB"-md5HP
    ${HADOOP_HOME}/bin/hadoop fs -rmr /hadoop/md5/$a"GB"-md5HP
    ${HADOOP_HOME}/bin/hadoop fs -mkdir -p /hadoop/md5/
    ${HADOOP_HOME}/bin/hadoop fs -put ./gen_data/$a"GB"-md5HP /hadoop/md5/
