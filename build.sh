#!/bin/bash
mvn compile
mvn package
cp target/land-claim-1.0-SNAPSHOT.jar ../server/plugins 
