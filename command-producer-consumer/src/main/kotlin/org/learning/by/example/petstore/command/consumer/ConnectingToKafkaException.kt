package org.learning.by.example.petstore.command.consumer

class ConnectingToKafkaException(cause: Throwable) : Exception(ERROR_CONNECTING, cause) {
    companion object {
        const val ERROR_CONNECTING = "Error connecting to kafka"
    }
}
