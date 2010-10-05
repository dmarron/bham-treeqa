#!/bin/bash

true_script_location=`readlink -fn $0`

pushd `dirname $true_script_location`

javaws -codebase file:web-dist/app -import web-dist/app/bham.jnlp

popd
