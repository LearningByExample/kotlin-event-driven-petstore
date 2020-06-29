#!/bin/bash

set -o errexit

TOPIC="$1"
MSG="$2"

echo "$MSG" | /usr/bin/kafka-console-producer --broker-list localhost:9092 --topic "$TOPIC"
