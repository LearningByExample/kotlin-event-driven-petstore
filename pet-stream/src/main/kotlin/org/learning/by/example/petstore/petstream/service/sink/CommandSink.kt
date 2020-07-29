package org.learning.by.example.petstore.petstream.service.sink

import org.learning.by.example.petstore.command.consumer.CommandsConsumer
import org.learning.by.example.petstore.petstream.service.handler.CommandHandler
import org.springframework.stereotype.Service
import reactor.core.Disposable
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class CommandSink(val commandsConsumer: CommandsConsumer, val commandHandler: CommandHandler) {
    lateinit var disposable: Disposable

    @PostConstruct
    fun run() {
        disposable = commandsConsumer.receiveCommands(commandHandler::handle).subscribe()
    }

    @PreDestroy
    fun end() {
        if (this::disposable.isInitialized) {
            disposable.dispose()
        }
    }
}
