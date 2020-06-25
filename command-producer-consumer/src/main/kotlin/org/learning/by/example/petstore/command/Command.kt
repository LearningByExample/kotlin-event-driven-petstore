package org.learning.by.example.petstore.command

import java.time.Instant
import java.util.UUID

data class Command(
    val commandName: String,
    val payload: Map<String, String>
) {
    val id: UUID = UUID.randomUUID()
    val timestamp: Instant = Instant.now()
}
