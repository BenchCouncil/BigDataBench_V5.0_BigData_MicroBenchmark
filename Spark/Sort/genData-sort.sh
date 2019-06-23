#!/bin/bash

# Generating command: ./genData-sort.sh <size>
#	size: the input data size, GB
  
#----------------------------genenrate-data----------------------------#
a=$1

    let L=a*2
    cd ../../BigDataGeneratorSuite/Text_datagen/
    rm -fr ./gen_data/sort-$a"G"
    ./gen_text_data.sh lda_wiki1w $L 8000 10000 ./gen_data/sort-$a"G"
    ${HADOOP_HOME}/bin/hadoop fs -rmr /spark/sort/sort-$a"G"
    ${HADOOP_HOME}/bin/hadoop fs -mkdir -p /spark/sort/
    ${HADOOP_HOME}/bin/hadoop fs -put ./gen_data/sort-$a"G" /spark/sort/
${HADOOP_HOME}/bin/hadoop fs -rmr /spark/sort/output
rm -fr ./gen_data/sort-$a"G"

