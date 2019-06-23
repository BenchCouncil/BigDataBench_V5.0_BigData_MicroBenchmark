#!/bin/bash
result=`./change-tripleMatrix $1 $2`
echo result:$result
sed -i -e "1i\
${result}" $2
