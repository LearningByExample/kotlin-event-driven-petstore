package org.learning.by.example.petstore.petcommands.model

import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size


data class Pet(
    @field:Size(min = 3, max = 20)
    @field:NotNull
    @field:Pattern(regexp = "[a-zAZ1-9]*", message = "should be alphanumeric")
    val name: String?,

    @field:Size(min = 3, max = 15)
    @field:NotNull
    @field:Pattern(regexp = "[a-zAZ\\-]*", message = "should be only alphabetic characters or hyphen")
    val category: String?,

    val tags: List<String>?
)
