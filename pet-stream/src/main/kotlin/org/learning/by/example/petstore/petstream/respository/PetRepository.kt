package org.learning.by.example.petstore.petstream.respository

import org.learning.by.example.petstore.petstream.model.Pet
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PetRepository : ReactiveCrudRepository<Pet, String>
