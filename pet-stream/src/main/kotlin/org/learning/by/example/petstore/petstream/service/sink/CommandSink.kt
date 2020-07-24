package org.learning.by.example.petstore.petstream.service.sink

import org.learning.by.example.petstore.command.consumer.CommandsConsumer
import org.learning.by.example.petstore.petstream.service.handler.CommandHandler
import org.springframework.stereotype.Service

@Service
class CommandSink(val commandsConsumer: CommandsConsumer, val commandHandler: CommandHandler) : Runnable {
    override fun run() {
        commandsConsumer.receiveCommands().flatMap(commandHandler::handle).blockLast()
    }
}
