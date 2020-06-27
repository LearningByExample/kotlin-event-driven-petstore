package org.learning.by.example.petstore.command.consumer

import org.learning.by.example.petstore.command.Command
import reactor.core.publisher.Flux

interface CommandsConsumer {
    fun receiveCommands(): Flux<Command>
}
