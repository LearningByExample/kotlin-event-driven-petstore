package org.learning.by.example.petstore.command.dsl

import org.learning.by.example.petstore.command.Command

class CommandDsl(private val commandName: String) {
    private val payload: HashMap<String, Any> = hashMapOf()

    fun build(): Command = Command(commandName, payload)

    infix fun String.value(value: Any) {
        payload[this] = value
    }
}

fun command(commandName: String, init: CommandDsl.() -> Unit) = with(CommandDsl(commandName)) {
    apply(init)
    build()
}
