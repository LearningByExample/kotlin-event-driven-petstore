# pet-commands
This is a microservice to send pet commands

[![License: Apache2](https://img.shields.io/badge/license-Apache%202-blue.svg)](/LICENSE)

# Example request
To run this example you need to have a kafka instance running in localhost:9092
```shell script
 $ http POST :8080/pet <<<'
            {
              "name": "fluffy",
              "category": "dog",
              "breed": "german shepherd",
              "dob": "2020-06-28T00:00:00.0Z",
              "vaccines": [
                "rabies",
                "parvovirus",
                "distemper"
              ],
              "tags" : [
                "soft",
                "beauty",
                "good-boy"
              ]
            }
'
HTTP/1.1 201 Created
Content-Length: 45
Content-Type: application/json
Location: /pet/e5915e93-c983-4e8b-b872-1523965512b9

{
    "id": "e5915e93-c983-4e8b-b872-1523965512b9"
}
```

This will produce in Kafka:

```shell script
 $ bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic pet-commands --from-beginning
[2020-07-12 09:58:30,940] WARN [Consumer clientId=consumer-console-consumer-37923-1, groupId=console-consumer-37923] Error while fetching metadata with correlation id 2 : {pet-commands=LEADER_NOT_AVAILABLE} (org.apache.kafka.clients.NetworkClient)
{"commandName":"pet_create","payload":{"vaccines":["rabies","parvovirus","distemper"],"dob":"2020-06-28T00:00:00Z","name":"fluffy","category":"dog","breed":"german shepherd","tags":["soft","beauty","good-boy"]},"id":"e5915e93-c983-4e8b-b872-1523965512b9","timestamp":"2020-07-12T08:06:13.698607Z"}
```
# TBC

TBC
