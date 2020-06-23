package org.learning.by.example.petstore.command.producer

import org.learning.by.example.petstore.command.Command
import reactor.core.publisher.Mono
import java.util.*

interface PetCommandsProducer {
    fun sendCommand(command: Command): Mono<UUID>
}
