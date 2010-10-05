#!/bin/bash

true_script_location=`readlink -fn $0`

pushd `dirname $true_script_location`

for  i in `"ls" lib`;
do
  CLASSPATH=$CLASSPATH:dist/lib/$i;
done

# 1536M = 1.5G
java -Xmx1536m -enableassertions -jar dist/bham-0.3.0.jar

popd

