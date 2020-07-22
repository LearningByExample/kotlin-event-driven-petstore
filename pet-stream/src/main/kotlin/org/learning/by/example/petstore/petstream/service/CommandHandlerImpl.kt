package org.learning.by.example.petstore.petstream.service

import org.learning.by.example.petstore.command.Command
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CommandHandlerImpl(applicationContext: ApplicationContext) : CommandHandler {
    private val processors = hashMapOf<String, CommandProcessor>()

    init {
        applicationContext.getBeansOfType<CommandProcessor>().forEach {
            processors[it.value.getCommandName()] = it.value
        }
    }

    override fun handle(cmd: Command) = if (processors.containsKey(cmd.commandName)) {
        with(processors[cmd.commandName]!!) {
            if (validate(cmd)) {
                process(cmd)
            } else {
                Mono.error(CommandProcessingException("validation error on command: '$cmd'"))
            }
        }
    } else Mono.error(ProcessorNotFoundException("processor for command name '${cmd.commandName}' not found"))
}
