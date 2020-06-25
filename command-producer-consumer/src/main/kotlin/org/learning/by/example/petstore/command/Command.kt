package org.learning.by.example.petstore.command

import java.time.Instant
import java.util.UUID

data class Command(val commandName: String) {
    constructor(commandName: String, init: Command.() -> Unit) : this(commandName) {
        apply(init)
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
