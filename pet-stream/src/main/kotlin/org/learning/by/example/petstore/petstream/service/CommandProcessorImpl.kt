package org.learning.by.example.petstore.petstream.service

import org.learning.by.example.petstore.command.Command
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CommandProcessorImpl : CommandProcessor {
    override fun process(command: Mono<Command>): Mono<Void> {
        return Mono.empty()
    }
}
