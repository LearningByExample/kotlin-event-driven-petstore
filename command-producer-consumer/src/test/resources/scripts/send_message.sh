#!/bin/bash
echo "$2" | /usr/bin/kafka-console-producer --broker-list localhost:9092 --topic "$1"
