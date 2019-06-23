#!/bin/bash

rm -f *.class fft.jar
javac -classpath `hadoop classpath` complex.java TextPair.java fft.java
cd ../..
jar cvf fft.jar org/fft/*.class
cd org/fft
mv ../../fft.jar .
hadoop fs -rmr out*
