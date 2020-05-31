package org.learning.by.example.petstore.petcommands.model

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


data class Pet(
    @get:NotNull @get:Size(min = 3, max = 64)
    val name: String,

    @get:NotNull @get:Size(min = 3, max = 64)
    val category: String
)
