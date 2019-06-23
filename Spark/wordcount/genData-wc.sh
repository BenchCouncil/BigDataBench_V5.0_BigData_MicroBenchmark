#!/bin/bash

# Generating command: ./genData-wc.sh <size>
#	size: the input data size, GB
  
#--------------generate-data-----------------------
a=$1

    let L=a*2
    cd ../../BigDataGeneratorSuite/Text_datagen/
    rm -fr ./gen_data/wordcount-$a"G"
    ./gen_text_data.sh lda_wiki1w $L 8000 10000 ./gen_data/wordcount-$a"G"
    ${HADOOP_HOME}/bin/hadoop fs -rmr /spark/wordcount/wordcount-$a"G"
    ${HADOOP_HOME}/bin/hadoop fs -mkdir -p /spark/wordcount/
    ${HADOOP_HOME}/bin/hadoop fs -put ./gen_data/wordcount-$a"G" /spark/wordcount/
${HADOOP_HOME}/bin/hadoop fs -rmr /spark/wordcount/output
rm -fr ./gen_data/wordcount-$a"G"
