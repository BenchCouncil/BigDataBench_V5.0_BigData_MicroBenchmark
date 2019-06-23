#!/bin/bash
echo "print data size GB :"
read GB
a=${GB}
let L=a*140
java -XX:NewRatio=1 -jar pdgf.jar -l demo-schema.xml -l demo-generation.xml -c -s -sf $L
