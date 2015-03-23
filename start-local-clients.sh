#!/usr/bin/env bash

for i in $(eval echo {1..$1})
do
	java -classpath ./out/production/FDDG-server nl.tud.dcs.fddg.StartClient $2 &
	sleep 0.2
done
