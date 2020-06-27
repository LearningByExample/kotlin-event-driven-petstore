#!/bin/bash
/usr/bin/kafka-topics --delete --topic "$1"
/usr/bin/kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic "$1"
