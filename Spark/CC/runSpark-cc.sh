#!/bin/bash

# Running command: ./runSpark-cc.sh <log_vertex>
#       log_vertex: indicates the vertex of the generated data, means vertex = 2^log_vertex

  
I=$1

#--------------------------------------------run----------------------------#
$SPARK_HOME/bin/spark-submit --class cn.ac.ict.bigdatabench.ConnectedComponent cc-kmeans/JAR_FILE/bigdatabench-spark_1.3.0-hadoop_1.0.4.jar /spark/cc/Google_genGraph_$I.txt


echo "spark cc end"
