#!/bin/bash
source /etc/profile
cd $BigdataBench_HomeSP/BigDataGeneratorSuite/Text_datagen/
HaHa
let L=a*2
rm -fr ./gen_data/$a"GB"-sortSP
sh gen_text_data.sh lda_wiki1w $L 8000 10000 ./gen_data/$a"GB"-sortSP
cd ./gen_data/$a"GB"-sortSP
rename lda `hostname` lda_*
${HADOOP_HOME}/bin/hadoop fs -rmr /spark/sort/$a"GB"-sortSP
${HADOOP_HOME}/bin/hadoop fs -mkdir -p /spark/sort
${HADOOP_HOME}/bin/hadoop fs -put ./gen_data/$a"GB"-sortSP /spark/sort
