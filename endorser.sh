#!/bin/bash

usage() {
	echo "usage: $0 <number-of-endorsers>"
}

# check if number of arguments is right
if [ "$#" -lt 1 ];
then
	usage	
	exit 1
fi

for ((ID=0; ID<$1; ID++))
do
	gnome-terminal --tab --title="Endorser $ID" -- java -Djava.security.manager -Djava.security.policy==config/endorser/endorser.policy -cp target/WA-1-jar-with-dependencies.jar server.endorser.BFTEndorser $ID 
done