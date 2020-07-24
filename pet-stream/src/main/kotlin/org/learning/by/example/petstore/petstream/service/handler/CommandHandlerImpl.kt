package org.learning.by.example.petstore.petstream.service.handler

import org.learning.by.example.petstore.command.Command
import org.learning.by.example.petstore.petstream.service.processor.CommandProcessor
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CommandHandlerImpl(applicationContext: ApplicationContext) : CommandHandler {
    private val processors = applicationContext.getBeansOfType<CommandProcessor>().map {
        it.value.getCommandName() to it.value
    }.toMap()

    override fun handle(cmd: Command) = if (cmd.commandName in processors) {
        with(processors.getValue(cmd.commandName)) {
            if (validate(cmd)) {
                process(cmd)
            } else {
                Mono.error(CommandHandlingException("validation error on command: '$cmd'"))
            }
        }
    } else Mono.error(ProcessorNotFoundException("processor for command name '${cmd.commandName}' not found"))
}
