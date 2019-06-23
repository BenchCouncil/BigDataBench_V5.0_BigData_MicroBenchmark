#!/bin/bash
##put talbes data to data file
rm -fr metastore_db
rm -fr derby.log
rm -fr TempStatsStore
WORK_DIR=`pwd`
cd ../../BigDataGeneratorSuite/Table_datagen/e-com
rm -rf output/*
./generate_table.sh
mv output/* $WORK_DIR/data
cd $WORK_DIR
hadoop dfs -rmr /hive
hadoop dfs -mkdir /hive
hadoop dfs -mkdir /hive/item
hadoop dfs -mkdir /hive/order
hadoop dfs -put data/OS_ORDER.txt /hive/order/
hadoop dfs -put data/OS_ORDER_ITEM.txt /hive/item/
hive -f hive.sql
