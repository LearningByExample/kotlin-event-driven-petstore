package org.learning.by.example.petstore.command

import java.time.Instant
import java.util.UUID

data class Command(
    val id: UUID,
    val timestamp: Instant,
    val eventName: String,
    val payload: Map<String, String>
)
