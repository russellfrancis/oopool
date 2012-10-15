#!/bin/bash
for i in 1 2 3 4 5 6 7 8 9 10; do
  python DocumentConverter.py input.doc output$i.pdf 2>&1 &
done;
