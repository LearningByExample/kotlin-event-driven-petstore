package org.learning.by.example.petstore.petstream.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("pets")
data class Pet(
    @field:Id
    val id: String,
    val name: String,
    val dob: Instant
)
