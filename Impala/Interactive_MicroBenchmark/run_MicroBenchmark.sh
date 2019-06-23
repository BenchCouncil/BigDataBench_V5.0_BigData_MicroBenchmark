#!/bin/bash
for i in projection filter aggregationAVG aggregationSUM aggregationMAX aggregationMIN orderby crossproduct union difference 
do
	#create result table in hive
	hive -f set-$i.sql
        hadoop fs -chmod -R 777 /user/hive/warehouse/result
	if [ $i = difference ]
	then
		hadoop fs -chmod -R 777 /user/hive/warehouse/item_temp
	fi
	#refresh impala by restart
        ./impala-restart.sh
	sleep 30
        nohup impala-shell -f $i.sql > output/impala-$i.out
	#free memory
        sleep 10
done
