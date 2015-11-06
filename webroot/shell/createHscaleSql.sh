#!/bin/bash
rm -rf createHscaleTables.sql;
for i in $(seq 0 99)
do
  file=`sed "s#&#$i#g" hscaleTables.sql`
  echo $file >> createHscaleTables.sql;
done