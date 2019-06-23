#!/bin/bash
source /etc/profile
cd $BigdataBench_HomeHP/BigDataGeneratorSuite/Text_datagen/
HaHa
let L=a*2
rm -fr ./gen_data/$a"GB"-wordcountHP
sh gen_text_data.sh lda_wiki1w $L 8000 10000 ./gen_data/$a"GB"-wordcountHP
${HADOOP_HOME}/bin/hadoop fs -rmr /hadoop/wd/$a"GB"-wordcountHP
${HADOOP_HOME}/bin/hadoop fs -mkdir -p /hadoop/wd
cd ./gen_data/$a"GB"-wordcountHP/
rename lda `hostname` lda_*
cd -
${HADOOP_HOME}/bin/hadoop fs -put ./gen_data/$a"GB"-wordcountHP /hadoop/wd
