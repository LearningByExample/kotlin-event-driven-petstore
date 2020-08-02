package org.learning.by.example.petstore.petqueries.service

import org.learning.by.example.petstore.petqueries.model.Pet
import reactor.core.publisher.Mono
import java.util.UUID

interface PetService {
    fun findPetById(id: UUID): Mono<Pet>
}
