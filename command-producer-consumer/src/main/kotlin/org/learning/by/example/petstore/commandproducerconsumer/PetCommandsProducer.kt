package org.learning.by.example.petstore.commandproducerconsumer

import reactor.core.publisher.Mono

interface PetCommandsProducer {
    fun sendCommand(command: Any): Mono<String>
}
