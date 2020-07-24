package org.learning.by.example.petstore.petstream.service.handler

import org.learning.by.example.petstore.command.Command
import reactor.core.publisher.Mono

interface CommandHandler {
    fun handle(cmd: Command): Mono<Void>
}
