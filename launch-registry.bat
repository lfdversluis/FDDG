@echo off

echo Setting CLASSPATH...
set CLASSPATH=.\out\production\FDDG-server;

echo Launching RMI registry...
rmiregistry
pause