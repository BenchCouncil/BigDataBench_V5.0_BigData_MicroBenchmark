#!/bin/bash

# Generating command: ./genData-md5.sh <size>
#	size: the input data size, GB
  
#----------------------------genenrate-data----------------------------#
a=$1

    let L=a*2
    cd ../../BigDataGeneratorSuite/Text_datagen/
    rm -fr ./gen_data/md5-$a"G"
    ./gen_text_data.sh lda_wiki1w $L 8000 10000 ./gen_data/md5-$a"G"
    ${HADOOP_HOME}/bin/hadoop fs -rmr /spark/md5/md5-$a"G"
    ${HADOOP_HOME}/bin/hadoop fs -mkdir -p /spark/md5/
    ${HADOOP_HOME}/bin/hadoop fs -put ./gen_data/md5-$a"G" /spark/md5/
${HADOOP_HOME}/bin/hadoop fs -rmr /spark/md5/output
rm -fr ./gen_data/md5-$a"G"

