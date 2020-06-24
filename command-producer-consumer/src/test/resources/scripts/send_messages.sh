#!/bin/bash
/usr/bin/kafka-topics --delete --topic pet-commands
/usr/bin/kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic pet-commands
/usr/bin/kafka-console-producer --broker-list localhost:9092 --topic pet-commands < "$1"
