#!/bin/bash
source /etc/profile
cd $BigdataBench_HomeSP/BigDataGeneratorSuite/Text_datagen/
HaHa
let L=a*2
rm -fr ./gen_data/$a"GB"-wordcountSP
sh gen_text_data.sh lda_wiki1w $L 8000 10000 ./gen_data/$a"GB"-wordcountSP
${HADOOP_HOME}/bin/hadoop fs -rmr /spark/wd/$a"GB"-wordcountSP
${HADOOP_HOME}/bin/hadoop fs -mkdir -p /spark/wd
cd ./gen_data/$a"GB"-wordcountSP
rename lda `hostname` lda_*
${HADOOP_HOME}/bin/hadoop fs -put ./gen_data/$a"GB"-wordcountSP /spark/wd
