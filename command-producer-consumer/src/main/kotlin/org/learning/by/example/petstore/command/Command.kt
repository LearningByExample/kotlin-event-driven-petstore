package org.learning.by.example.petstore.command

import java.time.Instant
import java.util.UUID

data class Command(val commandName: String) {
    companion object {
        fun create(commandName: String, init: Command.() -> Unit): Command {
            val command = Command(commandName)
            command.apply(init)
            return command
        }
    }

    val id: UUID = UUID.randomUUID()
    val timestamp: Instant = Instant.now()
    val payload: HashMap<String, Any> = hashMapOf()

    operator fun Pair<String, Any>.unaryPlus() {
        payload[this.first] = this.second
    }

    inline fun <reified T : Any> getOrElse(name: String, elseValue: T): T =
        with(payload.getOrDefault(name, elseValue)) {
            if (this is T) {
                this
            } else {
                elseValue
            }
        }
}
