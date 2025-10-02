#!/bin/bash
set -x
LIB=${0%/*}
java -cp $LIB/target/ava-0.3.jar:$LIB/jline-3.11.0.jar:$LIB/lanterna-3.0.3.jar Main
