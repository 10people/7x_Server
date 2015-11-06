#!/bin/bash
for i in $(seq 0 99)
do
  sed "s#&#$i#g" hscaleTables.sql |  mysql -f -u$1 -p$2 -h$3 $4
done 