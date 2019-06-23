#!/bin/bash
##
# Micro Benchmarks Workload: sort, grep, wordcount
# Need HADOOP 
# To prepare and generate data:
#  ./genData_MicroBenchmarks.sh
# To run:  
#  ./run_MicroBenchmarks.sh
##

if [ ! -e $HADOOP ]; then
  echo "Can't find hadoop in $HADOOP, exiting"
  exit 1
fi

WORK_DIR=`pwd`

algorithm=(aggregationAVG aggregationMAX aggregationMIN aggregationSUM crossproject difference filter orderby projection union)
if [ -n "$1" ]; then
  choice=$1
else
  echo "Please select a number to choose the corresponding Workload algorithm"
  echo "1. ${algorithm[0]} Workload"
  echo "2. ${algorithm[1]} Workload"
  echo "3. ${algorithm[2]} Workload"
  echo "4. ${algorithm[3]} Workload"
  echo "5. ${algorithm[4]} Workload"
  echo "6. ${algorithm[5]} Workload"
  echo "7. ${algorithm[6]} Workload"
  echo "8. ${algorithm[7]} Workload"
  echo "9. ${algorithm[8]} Workload"
  echo "10. ${algorithm[9]} Workload"
  read -p "Enter your choice : " choice
fi

echo "ok. You chose $choice and we'll use ${algorithm[$choice-1]} Workload"
Workloadtype=${algorithm[$choice-1]} 

if [ "x$Workloadtype" == "xaggregationAVG" ]; then
  hive -f BigOP-e-commerce-aggregationAVG.sql

elif [ "x$Workloadtype" == "xaggregationMAX" ]; then
  hive -f BigOP-e-commerce-aggregationMAX.sql

elif [ "x$Workloadtype" == "xaggregationMIN" ]; then
  hive -f BigOP-e-commerce-aggregationMIN.sql

elif [ "x$Workloadtype" == "xaggregationSUM" ]; then
  hive -f BigOP-e-commerce-aggregationSUM.sql

 elif [ "x$Workloadtype" == "xcrossproject" ]; then
  hive -f BigOP-e-commerce-crossproject.sql

elif [ "x$Workloadtype" == "xdifference" ]; then
  hive -f BigOP-e-commerce-difference.sql

elif [ "x$Workloadtype" == "xfilter" ]; then
  hive -f BigOP-e-commerce-filter.sql

elif [ "x$Workloadtype" == "xorderby" ]; then
  hive -f BigOP-e-commerce-orderby.sql

elif [ "x$Workloadtype" == "xprojection" ]; then
  hive -f BigOP-e-commerce-projection.sql

elif [ "x$Workloadtype" == "xunion" ]; then
  hive -f BigOP-e-commerce-union.sql
fi 
