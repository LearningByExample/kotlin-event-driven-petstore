package org.learning.by.example.petstore.petcommands.model

import javax.validation.constraints.Size


data class Pet(
    @field:Size(min = 3, max = 64)
    val name: String,

    @field:Size(min = 3, max = 64)
    val category: String
)
