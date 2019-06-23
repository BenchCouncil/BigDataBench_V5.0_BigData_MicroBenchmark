#!/bin/bash

# Generating command: ./genData-grep.sh <size>
#       size: the input data size, GB

  
#----------------------------genenrate-data----------------------------#
curdir=`pwd`
a=$1
    let L=a*2
    cd ../../BigDataGeneratorSuite/Text_datagen/
    rm -fr ./gen_data/grep-$a"G"
    ./gen_text_data.sh lda_wiki1w $L 8000 10000 ./gen_data/grep-$a"G"
    ${HADOOP_HOME}/bin/hadoop fs -rmr /spark/grep/grep-$a"G"
    ${HADOOP_HOME}/bin/hadoop fs -mkdir -p /spark/grep/
    ${HADOOP_HOME}/bin/hadoop fs -put ./gen_data/grep-$a"G" /spark/grep/
${HADOOP_HOME}/bin/hadoop fs -rmr /spark/grep/output
rm -fr ./gen_data/grep-$a"G"
