package org.learning.by.example.petstore.command.producer

import reactor.core.publisher.Mono

interface PetCommandsProducer {
    fun sendCommand(command: Any): Mono<String>
}
