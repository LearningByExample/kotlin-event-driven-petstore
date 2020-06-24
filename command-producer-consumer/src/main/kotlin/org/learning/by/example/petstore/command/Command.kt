package org.learning.by.example.petstore.command

import java.sql.Timestamp
import java.util.UUID

data class Command(
    val id: UUID,
    val timestamp: Timestamp,
    val eventName: String,
    val payload: Map<String, String>
)
