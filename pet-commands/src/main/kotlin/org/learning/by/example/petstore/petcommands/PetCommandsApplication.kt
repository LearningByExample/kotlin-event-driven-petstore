package org.learning.by.example.petstore.petcommands

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PetCommandsApplication

fun main(args: Array<String>) {
    runApplication<PetCommandsApplication>(*args)
}
