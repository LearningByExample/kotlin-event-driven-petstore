package org.learning.by.example.petstore.petcommands.model

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


data class Pet(
    @field:Size(min = 3, max = 64)
    @field:NotNull
    val name: String?,

    @field:Size(min = 3, max = 64)
    @field:NotNull
    val category: String?,

    val tags: List<String>?
)
