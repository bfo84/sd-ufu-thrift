#!/bin/bash

rm logs/* # cleaning log files

N=$1
echo "Initializing "$N" servers" > logs/log.txt
firstPort=$2
echo "Server initial port "$firstPort"" > logs/log.txt
id=0

while [ $id -lt $N ]; do
	echo Running java GraphServer $N $id $firstPort >> logs/log.txt
	filename="logServer"$id".txt"
	serverPort=$((firstPort+id))
	filePath=logs/$filename
	mvn exec:java -Dexec.mainClass=br.ufu.miguelpereira.view.Server -Dexec.args="$N $id $serverPort)"  > $filePath &
	echo "Server: "$id" port" $serverPort
	((id++))
done

id=0
echo "Opened ports: "
echo "Opened ports: " >> logs/log.txt
while [ $id -lt $N ]; do
	port=$((firstPort+id))
	echo "-----" $port
	echo "-----" $port >> logs/log.txt
	((id++))
done 
