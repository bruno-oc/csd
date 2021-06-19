## Confiabilidade de Sistemas Distribuidos

###**Blockchained Global Wallet Service**
A decentralized service for a distributed and replicated ledger of registration and control of operations of clients, based on a consistent replication architecture, with multiple servers and with byzantine fault tolerance guarantee.

#####**Deployment Guide**

On the run directory are provided some scripts that facilitate the testing and deployment of the servers.

* To build the jar with the needed dependencies:

		./run/build.sh

* To launch N **BFT Replicas**:

		./run/replica.sh N

* To launch a **single BFT Replica** with the identifier ID:

		./run/replica_single.sh ID

* To launch N **BGWS Endorsers**:

		./run/endorser.sh N

* To launch a **single BGWS Endorsers** with the identifier ID:

		./run/endorser_single.sh ID

* To launch a **BGWS Server** with identifier ID and running at ADDRESS:PORT :

		./run/server.sh ID ADDRESS PORT

* To launch a **BGWS Client** with identifier ID:

		./run/server.sh ADDRESS PORT ID
	where ADDRESS PORT correspond to the ADDRESS:PORT of a BGWS Server