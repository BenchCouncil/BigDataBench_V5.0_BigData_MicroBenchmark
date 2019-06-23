#!/bin/bash
##
# To prepare and generate data:
#  ./genData_randsample.sh
# To run:  
#  ./run_randsample.sh
##


echo "Preparing MicroBenchmarks data dir"

WORK_DIR=`pwd`
echo "WORK_DIR=$WORK_DIR data will be put in $WORK_DIR/data-randsample"

mkdir -p ${WORK_DIR}/data-randsample/

cd ../../BigDataGeneratorSuite/Text_datagen/

echo "print data size GB :"
read GB
a=${GB}
let L=a*2
./gen_text_data.sh lda_wiki1w $L 8000 10000 ${WORK_DIR}/data-randsample/



