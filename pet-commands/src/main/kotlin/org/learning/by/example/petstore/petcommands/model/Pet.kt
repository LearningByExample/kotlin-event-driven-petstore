package org.learning.by.example.petstore.petcommands.model

import javax.validation.constraints.Pattern

data class Pet(
    @Pattern(regexp = "[a-z]")
    var name: String,
    var category: String = ""
    // var tags: List<String> = listOf()
)


