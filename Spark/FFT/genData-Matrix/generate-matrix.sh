#!/bin/bash

file="data-kmeans"
rm -f $file
if [  -f "$file" ]; then
        echo "$file file exists"
        exit -1
fi

if [ $1 = "int" ];then
./generate-matrix-int $2 $3 $4
elif [ $1 = "float" ];then
./generate-matrix-float $2 $3 $4
                      #row  col  blank
else
echo "only int or float is accpeted"
fi
