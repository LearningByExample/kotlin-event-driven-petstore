package org.learning.by.example.petstore.commandproducerconsumer

import reactor.core.publisher.Mono

interface PetCommands {
    fun sendPetCreate(pojo: Any): Mono<String>
}
