#!/bin/bash

usage() {
	echo "usage: $0 <endorser-id>"
}

# check if number of arguments is right
if [ "$#" -lt 1 ];
then
	usage	
	exit 1
fi

gnome-terminal --tab --title="Endorser $1" -- java -Djava.security.manager -Djava.security.policy==config/endorser/endorser.policy -cp target/WA-1-jar-with-dependencies.jar server.endorser.BFTEndorser $1