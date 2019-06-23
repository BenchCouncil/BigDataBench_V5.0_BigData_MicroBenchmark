algorithm=( Sort Grep Wordcount)
if [ -n "$1" ]; then
  choice=$1
else
  echo "Please select a number to choose the corresponding Workload algorithm"
  echo "1. ${algorithm[0]} Workload"
  echo "2. ${algorithm[1]} Workload"
  echo "3. ${algorithm[2]} Workload"

  read -p "Enter your choice : " choice
fi

echo "ok. You chose $choice and we'll use ${algorithm[$choice-1]} Workload"
Workloadtype=${algorithm[$choice-1]}

if [ "x$Workloadtype" == "xSort" ]; then
#${HADOOP_HOME}/bin/hadoop fs -rmr /spark-sort-result
echo "The Sort workload is not stable currently, please choose another benchmark"

elif [ "x$Workloadtype" == "xGrep" ]; then
echo "the keyword to filter the text K:"
read K
${HADOOP_HOME}/bin/hadoop fs -rmr /flink-grep-result
$FLINK_HOME/bin/flink run --class cn.ac.ict.bigdatabench.Grep ../JAR_FILE/bigdatabench-flink-0.10.0.jar $HDFS/data-MicroBenchmarks ${K} $HDFS/flink-grep-result

elif [ "x$Workloadtype" == "xWordcount" ]; then
${HADOOP_HOME}/bin/hadoop fs -rmr /flink-wordcount-result
$FLINK_HOME/bin/flink run --class cn.ac.ict.bigdatabench.WordCount ../JAR_FILE/bigdatabench-flink-0.10.0.jar $HDFS/data-MicroBenchmarks $HDFS/flink-wordcount-result
fi
