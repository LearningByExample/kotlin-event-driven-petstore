#!/bin/bash

set -o errexit

TOPIC="$1"
OFFSET=$2

/usr/bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic "$TOPIC" --partition 0 --offset $OFFSET  \
    --max-messages 1
