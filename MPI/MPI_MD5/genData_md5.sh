#!/bin/bash
##
# To prepare and generate data:
#  ./genData_md5.sh
# To run:  
#  ./run_md5.sh
##


echo "Preparing MicroBenchmarks data dir"

WORK_DIR=`pwd`
echo "WORK_DIR=$WORK_DIR data will be put in $WORK_DIR/data-md5"

mkdir -p ${WORK_DIR}/data-md5/

cd ../../BigDataGeneratorSuite/Text_datagen/

echo "print data size GB :"
read GB
a=${GB}
let L=a*2
./gen_text_data.sh lda_wiki1w $L 8000 10000 ${WORK_DIR}/data-md5/



