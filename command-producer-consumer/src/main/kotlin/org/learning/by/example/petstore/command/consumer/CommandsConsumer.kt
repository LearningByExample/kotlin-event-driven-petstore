package org.learning.by.example.petstore.command.consumer

import reactor.core.publisher.Flux

interface CommandsConsumer {
    fun receiveCommands(): Flux<String>
}
