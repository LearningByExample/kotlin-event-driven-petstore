package org.learning.by.example.petstore.petstream.service.processor

import org.learning.by.example.petstore.command.Command
import reactor.core.publisher.Mono

interface CommandProcessor {
    fun process(cmd: Command): Mono<Void>
    fun getCommandName(): String
    fun validate(cmd: Command): Boolean
}
