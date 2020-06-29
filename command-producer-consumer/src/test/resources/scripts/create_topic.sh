#!/bin/bash

set -o errexit

TOPIC="$1"

/usr/bin/kafka-topics --bootstrap-server localhost:9092 --delete --topic "$TOPIC" || true #ignore fails if does no exist
/usr/bin/kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic "$TOPIC"
