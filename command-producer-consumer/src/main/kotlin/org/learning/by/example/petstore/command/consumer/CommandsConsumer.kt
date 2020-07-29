package org.learning.by.example.petstore.command.consumer

import org.learning.by.example.petstore.command.Command
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CommandsConsumer {
    fun receiveCommands(sink: (Command) -> Mono<Void>): Flux<Void>
}
