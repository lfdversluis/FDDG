@echo off

echo Killing existing RMI registry process..
taskkill /F /IM rmiregistry.exe
start launch-registry.bat