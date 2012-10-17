#!/bin/bash
for i in {1..100}; do
  python DocumentConverter.py input.txt tmp/output$i.pdf 2>&1 &
done;
