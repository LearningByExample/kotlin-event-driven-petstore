package org.learning.by.example.petstore.petstream.model

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("pets")
class Pet(
    @field:Id
    @field:Column("id")
    val _id: String,
    val name: String,
    val dob: Instant
) : Persistable<String> {
    override fun isNew(): Boolean {
        return true
    }
    override fun getId(): String? {
        return _id
    }
}
