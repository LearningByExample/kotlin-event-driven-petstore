#!/bin/bash
/usr/bin//kafka-console-consumer --bootstrap-server localhost:9092 --topic "$1" --partition 0 --offset $2  --max-messages 1
