#!/usr/bin/env bash

echo Killing RMI registry...
pkill rmiregistry
sleep 1
./launch-registry.sh
