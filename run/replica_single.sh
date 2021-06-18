#!/bin/bash

usage() {
	echo "usage: $0 <replica-id>"
}

# check if number of arguments is right
if [ "$#" -lt 1 ];
then
	usage	
	exit 1
fi

gnome-terminal --tab --title="Replica $1" -- java -cp target/WA-1-jar-with-dependencies.jar server.replica.BFTServer $1
