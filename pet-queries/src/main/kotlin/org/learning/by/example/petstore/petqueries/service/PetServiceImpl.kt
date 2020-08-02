package org.learning.by.example.petstore.petqueries.service

import org.learning.by.example.petstore.petqueries.model.Pet
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toMono
import java.util.UUID

@Service
class PetServiceImpl : PetService {
    override fun findPetById(id: UUID) =
        Pet("fluffy", "dog", "german shepherd", "2020-06-28T00:00:00.0Z").toMono()
}
