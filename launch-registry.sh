#!/usr/bin/env bash

echo Setting CLASSPATH...
setenv CLASSPATH ./out/production/FDDG-server;

echo Launching RMI registry...
rmiregistry