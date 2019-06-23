#!/bin/bash
for i in your impala nodes
do
	ssh $i "service impala-server stop;service impala-state-store stop;service impala-state-store start;service impala-server start" &
done
