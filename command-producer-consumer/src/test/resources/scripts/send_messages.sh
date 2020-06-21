#!/bin/bash
/usr/bin/kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test
/usr/bin/kafka-console-producer --broker-list localhost:9092 --topic test < $1
