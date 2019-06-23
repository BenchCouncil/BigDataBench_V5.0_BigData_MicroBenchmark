#!/bin/bash
for i in scan aggregation join 
do
	#create result table in hive
	hive -f set-$i.sql
        hadoop fs -chmod -R 777 /user/hive/warehouse/result
	#refresh impala by restart
        ./impala-restart.sh
	sleep 30
        nohup impala-shell -f $i.sql > output/impala-$i.out
	#free memory
        sleep 10
done
