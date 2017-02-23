@echo off
title InterfaceServer

javac -sourcepath ../../Component/MyComp -cp ../../Components/* ../../Components/MyComp/*.java
start "InterfaceServer" /D"../../Components/MyComp" java -cp .;../* InterfaceServer