package org.learning.by.example.petstore.petcommands.service

import org.learning.by.example.petstore.petcommands.model.Pet
import reactor.core.publisher.Mono
import java.util.UUID

interface PetCommands {
    fun sendPetCreate(pet: Pet): Mono<UUID>
}
