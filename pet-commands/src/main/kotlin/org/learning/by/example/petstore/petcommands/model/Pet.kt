package org.learning.by.example.petstore.petcommands.model

import org.learning.by.example.petstore.reactor.dtovalidator.AllElementsMatch
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
    @field:Pattern(regexp = "[a-zAZ]*", message = "should be only alphabetic characters")
    val category: String?,

    @field:AllElementsMatch(
        regexp = "[a-zA-Z\\-]{3,15}",
        message = "each should be between 3 and 15 alphabetic characters or hyphen"
    )
    val tags: List<String>?
)
