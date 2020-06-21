package org.learning.by.example.petstore.command.consumer

import reactor.core.publisher.Flux

interface PetCommandsConsumer {
    fun receiveCommands(): Flux<String>
}
