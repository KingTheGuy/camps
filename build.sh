#!/bin/bash
##BUILD
mvn compile
mvn package
####

##MOVE TO DEV SERVER
cp target/land-claim-1.0-SNAPSHOT.jar ../server/plugins 
####
