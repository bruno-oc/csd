#!/bin/bash

usage() {
	echo "usage: $0 <number-of-replicas>"
}

# check if number of arguments is right
if [ "$#" -lt 1 ];
then
	usage	
	exit 1
fi

for ((ID=0; ID<$1; ID++))
do
	gnome-terminal --tab --title="Replica $ID" -- java -cp target/WA-1-jar-with-dependencies.jar server.replica.BFTServer $ID 
done
