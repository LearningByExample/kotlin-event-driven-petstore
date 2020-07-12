package org.learning.by.example.petstore.petstream

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PetStreamApplication

fun main(args: Array<String>) {
    runApplication<PetStreamApplication>(*args)
}
