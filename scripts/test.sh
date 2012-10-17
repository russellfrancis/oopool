#!/bin/bash
CWD=`dirname $0`
cd $CWD
for i in {1..100}; do
  ./DocumentConverter.py input.txt ../tmp/output$i.pdf 2>&1 &
done;
